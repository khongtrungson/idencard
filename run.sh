#!/usr/bin/env bash
# ==============================================================================
# IdenCard Control Script
# Quản lý build và khởi chạy hệ thống IdenCard & Java Card Simulator
# ==============================================================================

set -e

# Màu sắc hiển thị
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Xác định thư mục dự án
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if [ -f "$SCRIPT_DIR/pom.xml" ] && [ -d "$SCRIPT_DIR/backend" ]; then
    PROJECT_DIR="$SCRIPT_DIR"
elif [ -d "$SCRIPT_DIR/idencard" ]; then
    PROJECT_DIR="$SCRIPT_DIR/idencard"
else
    # Mặc định đường dẫn tuyệt đối
    PROJECT_DIR="/home/trungson/Desktop/idencard"
fi

if [ ! -d "$PROJECT_DIR" ]; then
    echo -e "${RED}[ERROR] Không tìm thấy thư mục dự án tại: $PROJECT_DIR${NC}"
    exit 1
fi

AUTH_SERVER_DIR="$PROJECT_DIR/backend/auth-server"
SMARTCARD_DIR="$PROJECT_DIR/smartcard"
PORT_BACKEND=9000
PORT_SIMULATOR=9025

# Thiết lập các biến môi trường Java & JavaCard
export JAVA_HOME="${JAVA_HOME:-/usr/lib/jvm/java-21-openjdk-amd64}"
export JC_HOME_TOOLS="${JC_HOME_TOOLS:-/home/trungson/JavaCard/tools}"
export JC_HOME_SIMULATOR="${JC_HOME_SIMULATOR:-/home/trungson/JavaCard/simulator}"

print_header() {
    echo -e "${CYAN}======================================================${NC}"
    echo -e "${CYAN}         💳 IDENCARD MANAGEMENT SCRIPT               ${NC}"
    echo -e "${CYAN}======================================================${NC}"
    echo -e "Thư mục dự án: ${YELLOW}$PROJECT_DIR${NC}"
    echo -e "JAVA_HOME    : ${YELLOW}$JAVA_HOME${NC}"
    echo -e "JC_HOME_TOOLS: ${YELLOW}$JC_HOME_TOOLS${NC}\n"
}

check_prerequisites() {
    if ! command -v java &> /dev/null; then
        echo -e "${RED}[ERROR] Không tìm thấy 'java'. Vui lòng cài đặt JDK 21+${NC}"
        exit 1
    fi
    if ! command -v mvn &> /dev/null; then
        echo -e "${RED}[ERROR] Không tìm thấy 'mvn'. Vui lòng cài đặt Apache Maven${NC}"
        exit 1
    fi
}

build_all() {
    echo -e "${BLUE}==> Đang build toàn bộ module Maven (Clean Install)...${NC}"
    cd "$PROJECT_DIR"
    mvn clean install -DskipTests
    echo -e "${GREEN}==> Build thành công toàn bộ hệ thống!${NC}\n"
}

start_auth_server() {
    check_prerequisites
    echo -e "${BLUE}==> Đang khởi chạy Auth Server trên cổng $PORT_BACKEND...${NC}"
    echo -e "${YELLOW}URL: http://localhost:$PORT_BACKEND${NC}"
    echo -e "${YELLOW}OIDC Discovery: http://localhost:$PORT_BACKEND/.well-known/openid-configuration${NC}"
    echo -e "${YELLOW}H2 Console: http://localhost:$PORT_BACKEND/h2-console${NC}\n"
    cd "$AUTH_SERVER_DIR"
    mvn spring-boot:run
}

build_smartcard() {
    echo -e "${BLUE}==> [1/2] Biên dịch JavaCard Applet (theo tasks.json: Build Applet)...${NC}"
    mkdir -p "$SMARTCARD_DIR/bin"
    "$JAVA_HOME/bin/javac" -d "$SMARTCARD_DIR/bin" -g --release 10 -proc:none \
        -cp ".:$JC_HOME_TOOLS/lib/api_classic-3.2.0.jar:$JC_HOME_TOOLS/lib/tools.jar:$JC_HOME_TOOLS/lib/api_classic_annotations-3.2.0.jar" \
        $(find "$SMARTCARD_DIR/src" -name "*.java")

    echo -e "${BLUE}==> [2/2] Chuyển đổi và xác thực Applet (theo tasks.json: Convert and Verify Applet)...${NC}"
    cd "$SMARTCARD_DIR/src"
    local package_list="${package_list:-*}"
    shopt -s nullglob
    local file_list
    IFS=' ' read -r -a file_list <<< "$package_list"
    for i in "${file_list[@]}"; do
        "$JC_HOME_TOOLS/bin/converter.sh" -config $(find "../configurations" -name "${i}.conf")
    done
    for exp in $(find ../deliverables -name "*.exp"); do
        "$JC_HOME_TOOLS/bin/verifyexp.sh" "$exp"
    done
    echo -e "${GREEN}==> Biên dịch JavaCard Applet thành công! File CAP:${NC}"
    ls -l "$SMARTCARD_DIR/deliverables/com/idencard/applet/javacard/applet.cap" 2>/dev/null || true
    echo ""
}

deploy_smartcard_simulator() {
    echo -e "${BLUE}==> [1/2] Biên dịch DeployClient...${NC}"
    mkdir -p "$SMARTCARD_DIR/tools/bin"
    "$JAVA_HOME/bin/javac" -d "$SMARTCARD_DIR/tools/bin" \
        -cp "$JC_HOME_SIMULATOR/client/AMService/amservice.jar:$JC_HOME_SIMULATOR/client/COMService/socketprovider.jar" \
        "$SMARTCARD_DIR/tools/src/com/idencard/tools/DeployClient.java"

    local cap_file="$SMARTCARD_DIR/deliverables/com/idencard/applet/javacard/applet.cap"
    local config_file="$SMARTCARD_DIR/client.config.properties"

    if [ ! -f "$cap_file" ]; then
        echo -e "${YELLOW}[!] Chưa tìm thấy applet.cap, tiến hành build applet trước...${NC}"
        build_smartcard
    fi

    echo -e "${BLUE}==> [2/2] Nạp Applet lên Simulator (cổng $PORT_SIMULATOR)...${NC}"
    "$JAVA_HOME/bin/java" \
        -p "$JC_HOME_SIMULATOR/client/AMService/amservice.jar:$JC_HOME_SIMULATOR/client/COMService/socketprovider.jar" \
        --module-path "$JC_HOME_SIMULATOR/client/AMService:$JC_HOME_SIMULATOR/client/COMService" \
        --add-modules ALL-MODULE-PATH \
        -cp "$SMARTCARD_DIR/tools/bin" \
        com.idencard.tools.DeployClient "$cap_file" "$config_file"
}

start_and_deploy_simulator() {
    echo -e "${BLUE}==> [1/3] Kiểm tra trạng thái Java Card Simulator...${NC}"
    local sim_pid
    sim_pid=$(ss -tulpn 2>/dev/null | grep ":$PORT_SIMULATOR " | awk '{print $7}' | sed -E 's/.*pid=([0-9]+).*/\1/' || true)

    if [ -z "$sim_pid" ]; then
        echo -e "${YELLOW}Simulator chưa chạy. Đang tự động khởi chạy jcsl ngầm...${NC}"
        mkdir -p /tmp/idencard
        "$JC_HOME_SIMULATOR/runtime/bin/jcsl" > /tmp/idencard/simulator.log 2>&1 &
        sim_pid=$!
        echo -e "PID Simulator: $sim_pid (Log: /tmp/idencard/simulator.log)"

        # Đợi port mở
        local count=0
        while ! ss -tulpn 2>/dev/null | grep -q ":$PORT_SIMULATOR "; do
            sleep 0.5
            count=$((count + 1))
            if [ $count -gt 20 ]; then
                echo -e "${RED}[ERROR] Không khởi động được simulator trên cổng $PORT_SIMULATOR sau 10s!${NC}"
                cat /tmp/idencard/simulator.log
                return 1
            fi
        done
        echo -e "${GREEN}==> Simulator đã sẵn sàng trên cổng $PORT_SIMULATOR!${NC}"
    else
        echo -e "${GREEN}==> Simulator đã đang hoạt động trên cổng $PORT_SIMULATOR (PID: $sim_pid)${NC}"
    fi

    echo -e "\n${BLUE}==> [2/3] Kiểm tra mã nguồn và biên dịch Applet...${NC}"
    build_smartcard

    echo -e "${BLUE}==> [3/3] Nạp Applet vào Simulator...${NC}"
    deploy_smartcard_simulator

    echo -e "${GREEN}================================================================${NC}"
    echo -e "${GREEN}🎉 SIMULATOR & APPLET ĐÃ HOẠT ĐỘNG HOÀN TOÀN TRÊN CỔNG $PORT_SIMULATOR!${NC}"
    echo -e "  • Cổng kết nối Socket : ${YELLOW}localhost:$PORT_SIMULATOR${NC}"
    echo -e "  • Applet AID          : ${YELLOW}F00000000101020304${NC}"
    echo -e "  • Trạng thái          : ${GREEN}Sẵn sàng nhận lệnh APDU từ Android App${NC}"
    echo -e "  • Hướng dẫn           : Mở Android App và chọn ${CYAN}'Kết nối Card Simulator (Socket)'${NC} để test."
    echo -e "${GREEN}================================================================${NC}\n"
}

stop_simulator() {
    echo -e "${BLUE}==> Đang tìm và dừng Java Card Simulator (Port $PORT_SIMULATOR)...${NC}"
    local sim_pid
    sim_pid=$(ss -tulpn 2>/dev/null | grep ":$PORT_SIMULATOR " | awk '{print $7}' | sed -E 's/.*pid=([0-9]+).*/\1/' || true)
    if [ -n "$sim_pid" ]; then
        kill -9 "$sim_pid" 2>/dev/null || true
        echo -e "${GREEN}==> Đã tắt Java Card Simulator (PID: $sim_pid)${NC}"
    else
        local jcsl_pid
        jcsl_pid=$(pgrep -x jcsl 2>/dev/null || true)
        if [ -n "$jcsl_pid" ]; then
            kill -9 "$jcsl_pid" 2>/dev/null || true
            echo -e "${GREEN}==> Đã tắt jcsl (PID: $jcsl_pid)${NC}"
        else
            echo -e "${YELLOW}[INFO] Java Card Simulator hiện không chạy.${NC}"
        fi
    fi
}

clean_smartcard() {
    echo -e "${BLUE}==> Dọn dẹp bản build SmartCard (Clean)...${NC}"
    rm -rf "$SMARTCARD_DIR/bin" "$SMARTCARD_DIR/deliverables" "$SMARTCARD_DIR/tools/bin"
    echo -e "${GREEN}==> Đã dọn dẹp sạch sẽ bin, deliverables, tools/bin!${NC}\n"
}

check_status() {
    echo -e "${BLUE}==> [1/2] Kiểm tra Auth Server (Port $PORT_BACKEND)...${NC}"
    local auth_pid
    auth_pid=$(lsof -ti :$PORT_BACKEND 2>/dev/null || ss -tulpn 2>/dev/null | grep ":$PORT_BACKEND " | awk '{print $7}' | sed -E 's/.*pid=([0-9]+).*/\1/' || true)
    if [ -n "$auth_pid" ]; then
        echo -e "  ${GREEN}[RUNNING] Auth Server đang hoạt động (PID: $auth_pid)${NC}"
    else
        echo -e "  ${YELLOW}[STOPPED] Auth Server chưa chạy${NC}"
    fi

    echo -e "${BLUE}==> [2/2] Kiểm tra Java Card Simulator (Port $PORT_SIMULATOR)...${NC}"
    local sim_pid
    sim_pid=$(ss -tulpn 2>/dev/null | grep ":$PORT_SIMULATOR " | awk '{print $7}' | sed -E 's/.*pid=([0-9]+).*/\1/' || true)
    if [ -n "$sim_pid" ]; then
        echo -e "  ${GREEN}[RUNNING] Simulator đang hoạt động (PID: $sim_pid)${NC}"
    else
        echo -e "  ${YELLOW}[STOPPED] Simulator chưa chạy${NC}"
    fi
}

stop_server() {
    echo -e "${BLUE}==> Đang tìm và dừng Auth Server trên cổng $PORT_BACKEND...${NC}"
    local pid
    pid=$(lsof -ti :$PORT_BACKEND 2>/dev/null || ss -tulpn 2>/dev/null | grep ":$PORT_BACKEND " | awk '{print $7}' | sed -E 's/.*pid=([0-9]+).*/\1/' || true)
    if [ -n "$pid" ]; then
        kill -15 "$pid" 2>/dev/null || kill -9 "$pid" 2>/dev/null
        echo -e "${GREEN}==> Đã tắt Auth Server (PID: $pid)${NC}"
    else
        echo -e "${YELLOW}[INFO] Không tìm thấy Auth Server đang chạy trên cổng $PORT_BACKEND${NC}"
    fi
}

show_menu() {
    print_header
    echo -e "Chọn chức năng bạn muốn thực hiện:"
    echo -e "  ${GREEN}1)${NC} 🚀 Chạy trọn gói Simulator (Bật jcsl + Build & Deploy Applet cổng $PORT_SIMULATOR)"
    echo -e "  ${GREEN}2)${NC} 🌐 Khởi chạy Backend Auth Server (Spring Boot cổng $PORT_BACKEND)"
    echo -e "  ${GREEN}3)${NC} 📦 Build toàn bộ dự án Maven (Backend & Shared)"
    echo -e "  ${GREEN}4)${NC} 🎴 Chỉ biên dịch SmartCard Applet sinh file .cap"
    echo -e "  ${GREEN}5)${NC} 📲 Chỉ nạp lại Applet lên Simulator (DeployClient)"
    echo -e "  ${GREEN}6)${NC} 🛑 Dừng Java Card Simulator (Port $PORT_SIMULATOR)"
    echo -e "  ${GREEN}7)${NC} 🛑 Dừng Backend Auth Server (Port $PORT_BACKEND)"
    echo -e "  ${GREEN}8)${NC} 🧹 Dọn dẹp thư mục build SmartCard (Clean)"
    echo -e "  ${GREEN}9)${NC} 🔍 Kiểm tra trạng thái hệ thống (Cổng $PORT_BACKEND và $PORT_SIMULATOR)"
    echo -e "  ${GREEN}0)${NC} Thoát"
    echo ""
    read -p "Nhập lựa chọn của bạn [0-9]: " choice
    case "$choice" in
        1) start_and_deploy_simulator ;;
        2) start_auth_server ;;
        3) build_all ;;
        4) build_smartcard ;;
        5) deploy_smartcard_simulator ;;
        6) stop_simulator ;;
        7) stop_server ;;
        8) clean_smartcard ;;
        9) check_status ;;
        0) echo -e "${CYAN}Tạm biệt!${NC}"; exit 0 ;;
        *) echo -e "${RED}Lựa chọn không hợp lệ!${NC}"; exit 1 ;;
    esac
}

# Xử lý tham số dòng lệnh
case "$1" in
    sim|simulator)
        print_header
        start_and_deploy_simulator
        ;;
    stop-sim)
        print_header
        stop_simulator
        ;;
    deploy)
        print_header
        deploy_smartcard_simulator
        ;;
    smartcard|cap)
        print_header
        build_smartcard
        ;;
    clean-card|clean)
        print_header
        clean_smartcard
        ;;
    start|run|server)
        print_header
        start_auth_server
        ;;
    stop|stop-server)
        print_header
        stop_server
        ;;
    build)
        print_header
        build_all
        ;;
    status)
        print_header
        check_status
        ;;
    help|-h|--help)
        print_header
        echo -e "Cách dùng: $0 [lệnh]"
        echo -e "Các lệnh thường dùng nhất:"
        echo -e "  ${GREEN}sim${NC}            : Chạy trọn gói Simulator (Bật jcsl + nạp Applet cổng $PORT_SIMULATOR)"
        echo -e "  ${GREEN}stop-sim${NC}       : Tắt Simulator"
        echo -e "  ${GREEN}start | run${NC}    : Chạy Backend Auth Server (cổng $PORT_BACKEND)"
        echo -e "  ${GREEN}stop${NC}           : Tắt Backend Auth Server"
        echo -e "  ${GREEN}status${NC}         : Kiểm tra trạng thái cổng $PORT_BACKEND và $PORT_SIMULATOR"
        echo -e "  ${GREEN}(không tham số)${NC}: Mở menu tương tác"
        ;;
    "")
        show_menu
        ;;
    *)
        echo -e "${RED}[ERROR] Lệnh không hợp lệ: $1${NC}"
        echo -e "Dùng: $0 help để xem hướng dẫn."
        exit 1
        ;;
esac
