#!/usr/bin/env bash
# ==============================================================================
# SmartCard Applet Build Script (theo .vscode/tasks.json)
# ==============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
export JAVA_HOME="${JAVA_HOME:-/usr/lib/jvm/java-21-openjdk-amd64}"
export JC_HOME_TOOLS="${JC_HOME_TOOLS:-/home/trungson/JavaCard/tools}"
export JC_HOME_SIMULATOR="${JC_HOME_SIMULATOR:-/home/trungson/JavaCard/simulator}"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

clean() {
    echo -e "${YELLOW}==> Dọn dẹp thư mục bin, deliverables, tools/bin...${NC}"
    rm -rf "$SCRIPT_DIR/bin" "$SCRIPT_DIR/deliverables" "$SCRIPT_DIR/tools/bin"
    echo -e "${GREEN}==> Đã dọn dẹp xong.${NC}"
}

build_applet() {
    echo -e "${BLUE}==> [1/2] Biên dịch JavaCard Applet (javac)...${NC}"
    mkdir -p "$SCRIPT_DIR/bin"
    "$JAVA_HOME/bin/javac" -d "$SCRIPT_DIR/bin" -g --release 10 -proc:none \
        -cp ".:$JC_HOME_TOOLS/lib/api_classic-3.2.0.jar:$JC_HOME_TOOLS/lib/tools.jar:$JC_HOME_TOOLS/lib/api_classic_annotations-3.2.0.jar" \
        $(find "$SCRIPT_DIR/src" -name "*.java")

    echo -e "${BLUE}==> [2/2] Chuyển đổi và xác thực Applet (converter & verifyexp)...${NC}"
    cd "$SCRIPT_DIR/src"
    package_list="${package_list:-*}"
    shopt -s nullglob
    IFS=' ' read -r -a file_list <<< "$package_list"
    for i in "${file_list[@]}"; do
        "$JC_HOME_TOOLS/bin/converter.sh" -config $(find "../configurations" -name "${i}.conf")
    done
    for exp in $(find ../deliverables -name "*.exp"); do
        "$JC_HOME_TOOLS/bin/verifyexp.sh" "$exp"
    done
    echo -e "${GREEN}==> Build thành công! File .cap đã được tạo tại:${NC}"
    ls -l "$SCRIPT_DIR"/deliverables/com/idencard/applet/javacard/*.cap 2>/dev/null || true
}

deploy_applet() {
    echo -e "${BLUE}==> Biên dịch DeployClient...${NC}"
    mkdir -p "$SCRIPT_DIR/tools/bin"
    "$JAVA_HOME/bin/javac" -d "$SCRIPT_DIR/tools/bin" \
        -cp "$JC_HOME_SIMULATOR/client/AMService/amservice.jar:$JC_HOME_SIMULATOR/client/COMService/socketprovider.jar" \
        "$SCRIPT_DIR/tools/src/com/idencard/tools/DeployClient.java"

    CAP_FILE="$SCRIPT_DIR/deliverables/com/idencard/applet/javacard/applet.cap"
    if [ ! -f "$CAP_FILE" ]; then
        echo -e "${YELLOW}Chưa có applet.cap, tiến hành build trước...${NC}"
        build_applet
    fi

    echo -e "${BLUE}==> Nạp Applet lên Simulator...${NC}"
    "$JAVA_HOME/bin/java" \
        -p "$JC_HOME_SIMULATOR/client/AMService/amservice.jar:$JC_HOME_SIMULATOR/client/COMService/socketprovider.jar" \
        --module-path "$JC_HOME_SIMULATOR/client/AMService:$JC_HOME_SIMULATOR/client/COMService" \
        --add-modules ALL-MODULE-PATH \
        -cp "$SCRIPT_DIR/tools/bin" \
        com.idencard.tools.DeployClient "$CAP_FILE" "$SCRIPT_DIR/client.config.properties"
}

start_sim() {
    echo -e "${BLUE}==> Khởi chạy Java Card Simulator...${NC}"
    "$JC_HOME_SIMULATOR/runtime/bin/jcsl"
}

case "$1" in
    clean)
        clean
        ;;
    deploy)
        deploy_applet
        ;;
    sim|simulator)
        start_sim
        ;;
    build|"")
        build_applet
        ;;
    *)
        echo "Cách dùng: $0 [build|clean|deploy|sim]"
        exit 1
        ;;
esac
