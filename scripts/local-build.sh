#!/bin/bash
# Simplified local build script for Element Desktop
# This script builds Element Desktop for the current platform without
# requiring full infrastructure (Windows VMs, rsync servers, etc.)

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default values
VERSION="${1:-develop}"
SKIP_SYNC="true"
CURRENT_PLATFORM=""

echo -e "${BLUE}╔════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  Element Desktop - Local Build Script     ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════╝${NC}"
echo ""

# Detect current platform
detect_platform() {
    case "$(uname -s)" in
        Darwin*)
            CURRENT_PLATFORM="universal-apple-darwin"
            echo -e "${GREEN}✓${NC} Detected platform: macOS"
            ;;
        Linux*)
            CURRENT_PLATFORM="x86_64-unknown-linux-gnu"
            echo -e "${GREEN}✓${NC} Detected platform: Linux"
            ;;
        CYGWIN*|MINGW*|MSYS*)
            CURRENT_PLATFORM="x86_64-pc-windows-msvc"
            echo -e "${GREEN}✓${NC} Detected platform: Windows"
            ;;
        *)
            echo -e "${RED}✗${NC} Unknown platform: $(uname -s)"
            exit 1
            ;;
    esac
}

# Check prerequisites
check_prerequisites() {
    echo ""
    echo -e "${BLUE}Checking prerequisites...${NC}"

    # Check Node.js
    if command -v node &> /dev/null; then
        NODE_VERSION=$(node --version)
        echo -e "${GREEN}✓${NC} Node.js: $NODE_VERSION"
    else
        echo -e "${RED}✗${NC} Node.js not found. Please install Node.js 16+"
        exit 1
    fi

    # Check Yarn
    if command -v yarn &> /dev/null; then
        YARN_VERSION=$(yarn --version)
        echo -e "${GREEN}✓${NC} Yarn: $YARN_VERSION"
    else
        echo -e "${RED}✗${NC} Yarn not found. Please install Yarn"
        exit 1
    fi

    # Check Git
    if command -v git &> /dev/null; then
        GIT_VERSION=$(git --version | cut -d' ' -f3)
        echo -e "${GREEN}✓${NC} Git: $GIT_VERSION"
    else
        echo -e "${RED}✗${NC} Git not found. Please install Git"
        exit 1
    fi

    # Check Docker (for Linux builds on non-Linux)
    if [ "$CURRENT_PLATFORM" = "x86_64-unknown-linux-gnu" ]; then
        if command -v docker &> /dev/null; then
            echo -e "${GREEN}✓${NC} Docker: $(docker --version | cut -d' ' -f3)"
        else
            echo -e "${YELLOW}⚠${NC} Docker not found. Linux builds require Docker."
            echo -e "${YELLOW}  Install Docker Desktop or run on a Linux machine${NC}"
        fi
    fi
}

# Setup dummy environment variables
setup_dummy_env() {
    echo ""
    echo -e "${BLUE}Setting up environment...${NC}"

    # For local builds, we don't need real Windows VM credentials
    # Set dummy values to satisfy the builder's requirements
    export RIOTBUILD_WIN_VMNAME="${RIOTBUILD_WIN_VMNAME:-localhost}"
    export RIOTBUILD_WIN_USERNAME="${RIOTBUILD_WIN_USERNAME:-builder}"
    export RIOTBUILD_WIN_PASSWORD="${RIOTBUILD_WIN_PASSWORD:-dummy}"

    echo -e "${GREEN}✓${NC} Environment configured for local build"
}

# Build the project
build_project() {
    echo ""
    echo -e "${BLUE}Building Element Desktop...${NC}"
    echo -e "${YELLOW}Version:${NC} $VERSION"
    echo -e "${YELLOW}Target:${NC} $CURRENT_PLATFORM"
    echo ""

    # Check if lib directory exists
    if [ ! -d "lib" ]; then
        echo -e "${YELLOW}Building TypeScript...${NC}"
        yarn build
        echo -e "${GREEN}✓${NC} TypeScript build complete"
    else
        echo -e "${GREEN}✓${NC} Using existing TypeScript build"
    fi

    echo ""
    echo -e "${YELLOW}Starting Element Desktop build...${NC}"
    echo -e "${YELLOW}This may take 10-30 minutes depending on your machine${NC}"
    echo ""

    # Run the builder
    node lib/index.js \
        --version "$VERSION" \
        --targets "$CURRENT_PLATFORM" \
        --skip-sync

    BUILD_EXIT_CODE=$?

    if [ $BUILD_EXIT_CODE -eq 0 ]; then
        echo ""
        echo -e "${GREEN}╔════════════════════════════════════════════╗${NC}"
        echo -e "${GREEN}║  Build completed successfully!             ║${NC}"
        echo -e "${GREEN}╚════════════════════════════════════════════╝${NC}"
        echo ""
        show_artifacts
    else
        echo ""
        echo -e "${RED}╔════════════════════════════════════════════╗${NC}"
        echo -e "${RED}║  Build failed!                             ║${NC}"
        echo -e "${RED}╚════════════════════════════════════════════╝${NC}"
        exit $BUILD_EXIT_CODE
    fi
}

# Show build artifacts
show_artifacts() {
    echo -e "${BLUE}Build artifacts:${NC}"
    echo ""

    if [ -d "packages.riot.im" ]; then
        find packages.riot.im -type f \( \
            -name "*.dmg" -o \
            -name "*.deb" -o \
            -name "*.exe" -o \
            -name "*.msi" -o \
            -name "*.zip" -o \
            -name "*.nupkg" \
        \) -exec ls -lh {} \; | awk '{print "  " $9 " (" $5 ")"}'
    fi

    if [ -d "debian" ]; then
        echo ""
        echo -e "${BLUE}Debian packages also available in:${NC} debian/"
    fi

    echo ""
    echo -e "${YELLOW}Note:${NC} Artifacts are in 'packages.riot.im/' directory"
}

# Usage information
show_usage() {
    cat << EOF
Usage: $0 [VERSION]

Arguments:
  VERSION     Git branch, tag, or commit to build (default: develop)
              Examples: v1.11.50, develop, main

Examples:
  $0                    # Build develop branch
  $0 v1.11.50           # Build specific version
  $0 my-feature-branch  # Build feature branch

Environment Variables:
  RIOTBUILD_WIN_VMNAME     Windows VM name (default: localhost)
  RIOTBUILD_WIN_USERNAME   Windows username (default: builder)
  RIOTBUILD_WIN_PASSWORD   Windows password (default: dummy)

Notes:
  - This script builds for the current platform only
  - Windows VM credentials are not actually used for local builds
  - Builds are not synced to remote servers (--skip-sync is forced)
  - For full multi-platform builds, use the main builder or GitHub Actions

EOF
}

# Main execution
main() {
    if [ "$1" = "--help" ] || [ "$1" = "-h" ]; then
        show_usage
        exit 0
    fi

    detect_platform
    check_prerequisites
    setup_dummy_env
    build_project
}

main "$@"
