#!/bin/bash

# Android Studio 自带JDK的常见路径
ANDROID_STUDIO_JDK_PATHS=(
    "/Applications/Android Studio.app/Contents/jbr/Contents/Home"
    "/Applications/Android Studio 2.app/Contents/jbr/Contents/Home"
    "/Applications/Android Studio Preview.app/Contents/jbr/Contents/Home"
    "$HOME/Applications/Android Studio.app/Contents/jbr/Contents/Home"
    "/Applications/Android Studio.app/Contents/jre/Contents/Home"
    "/Applications/Android Studio 2.app/Contents/jre/Contents/Home"
)

# 查找可用的Android Studio JDK
find_android_studio_jdk() {
    for path in "${ANDROID_STUDIO_JDK_PATHS[@]}"; do
        if [ -d "$path" ] && [ -x "$path/bin/java" ]; then
            echo "$path"
            return 0
        fi
    done
    return 1
}

# 设置JAVA_HOME
setup_java_home() {
    local jdk_path=$(find_android_studio_jdk)
    
    if [ -n "$jdk_path" ]; then
        export JAVA_HOME="$jdk_path"
        export PATH="$JAVA_HOME/bin:$PATH"
        echo "✓ 使用Android Studio自带的JDK: $JAVA_HOME"
        echo "✓ Java版本: $("$JAVA_HOME/bin/java" -version 2>&1 | head -n 1)"
        echo ""
    else
        echo "✗ 未找到Android Studio自带的JDK"
        echo "请确保Android Studio已正确安装在以下位置之一:"
        for path in "${ANDROID_STUDIO_JDK_PATHS[@]}"; do
            echo "  - $path"
        done
        exit 1
    fi
}

# 显示帮助信息
show_help() {
    echo "用法: $0 [命令]"
    echo ""
    echo "可用命令:"
    echo "  debug    编译调试版本 (assembleDebug)"
    echo "  release  编译发布版本 (assembleRelease)"
    echo "  install  安装调试版本到设备 (installDebug)"
    echo "  test     运行测试 (test)"
    echo "  clean    清理构建 (clean)"
    echo "  help     显示此帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 debug    # 编译调试版本"
    echo "  $0 release  # 编译发布版本"
    echo ""
}

# 执行Gradle命令
run_gradle() {
    local command="$1"
    
    if [ ! -f "./gradlew" ]; then
        echo "✗ 未找到gradlew脚本，请确保在项目根目录下运行此脚本"
        exit 1
    fi
    
    # 确保gradlew有执行权限
    chmod +x ./gradlew
    
    echo "▶ 执行命令: ./gradlew $command"
    echo "========================================"
    echo ""
    
    ./gradlew "$command"
    
    local exit_code=$?
    
    echo ""
    echo "========================================"
    if [ $exit_code -eq 0 ]; then
        echo "✓ 命令执行成功"
    else
        echo "✗ 命令执行失败 (退出码: $exit_code)"
    fi
    
    exit $exit_code
}

# 主函数
main() {
    # 检查是否有参数
    if [ $# -eq 0 ]; then
        show_help
        exit 1
    fi
    
    # 解析命令
    case "$1" in
        debug|assembleDebug)
            setup_java_home
            run_gradle "assembleDebug"
            ;;
        release|assembleRelease)
            setup_java_home
            run_gradle "assembleRelease"
            ;;
        install|installDebug)
            setup_java_home
            run_gradle "installDebug"
            ;;
        test)
            setup_java_home
            run_gradle "test"
            ;;
        clean)
            setup_java_home
            run_gradle "clean"
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            echo "✗ 未知命令: $1"
            echo ""
            show_help
            exit 1
            ;;
    esac
}

# 执行主函数
main "$@"
