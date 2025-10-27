# Quick Start Guide

Get started building Element Desktop quickly for your current platform.

## Prerequisites

- **Node.js** 16+ (v18 recommended)
- **Yarn** 1.22+
- **Git** 2.0+
- **Docker** (for Linux builds on non-Linux systems)

## Quick Build

### Option 1: Simplified Local Build (Recommended for Testing)

Build Element Desktop for your current platform:

```bash
# Install dependencies
yarn install

# Build the TypeScript
yarn build

# Run the simplified build script
./scripts/local-build.sh

# Or build a specific version
./scripts/local-build.sh v1.11.50
```

This will:
- Auto-detect your platform (macOS, Linux, or Windows)
- Build only for your current platform
- Skip artifact syncing to remote servers
- Output build artifacts to `packages.riot.im/`

### Option 2: Using GitHub Actions (Recommended for Production)

1. **Push a tag** to trigger automatic builds:
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```

2. **Or manually trigger** a workflow:
   - Go to Actions tab in GitHub
   - Select "Build Element Desktop"
   - Click "Run workflow"
   - Enter version and targets

GitHub Actions will build for all platforms and create a draft release with all artifacts.

### Option 3: Full Builder (Advanced)

For complete multi-platform builds with all infrastructure:

```bash
# Set up environment
export RIOTBUILD_WIN_VMNAME="your-windows-vm"
export RIOTBUILD_WIN_USERNAME="build-user"
export RIOTBUILD_WIN_PASSWORD="secure-password"
export RIOTBUILD_RSYNC_ROOT="user@server:/artifacts"

# Build a release
node lib/index.js --version v1.11.50

# Or start nightly builder
node lib/index.js
```

See [PRODUCTION_BUILD_GUIDE.md](PRODUCTION_BUILD_GUIDE.md) for complete details.

## What Gets Built

Depending on your platform:

### macOS
- `packages.riot.im/desktop/install/macos/Element.dmg` - Installer
- `packages.riot.im/desktop/update/macos/*.zip` - Update package

### Linux
- `packages.riot.im/desktop/` - .deb packages
- `debian/` - Full Debian repository structure

### Windows
- `packages.riot.im/desktop/install/win32/{arch}/Element Setup.exe` - Installer
- `packages.riot.im/desktop/install/win32/{arch}/msi/Element Setup.msi` - MSI
- `packages.riot.im/desktop/update/win32/{arch}/` - Update packages

## Build Time

Typical build times on modern hardware:
- **macOS**: 15-25 minutes
- **Linux**: 10-20 minutes
- **Windows**: 20-30 minutes

First builds take longer due to dependency compilation.

## Troubleshooting

### "No windows credentials set"

For local builds, set dummy values:
```bash
export RIOTBUILD_WIN_VMNAME="localhost"
export RIOTBUILD_WIN_USERNAME="builder"
export RIOTBUILD_WIN_PASSWORD="dummy"
```

Or use the simplified `local-build.sh` script which sets these automatically.

### Docker errors (Linux builds)

Make sure Docker is running:
```bash
docker ps
```

If not installed, get Docker Desktop from https://docker.com

### Build fails with network errors

Retry the build - network issues during package downloads are common.

### Lock file exists

Remove the lock file:
```bash
rm element-builder.lock
```

## Next Steps

- 📖 Read [PRODUCTION_BUILD_GUIDE.md](PRODUCTION_BUILD_GUIDE.md) for comprehensive documentation
- 🔧 Customize GitHub Actions workflow in `.github/workflows/build-desktop.yml`
- 🚀 Set up CI/CD with automated builds
- 📦 Configure artifact storage (S3, rsync, etc.)
- 🔐 Set up code signing for production releases

## Platform-Specific Notes

### macOS
- Universal binary includes both Intel (x86_64) and Apple Silicon (arm64)
- Requires macOS to build (or GitHub Actions macos-latest runner)
- Code signing requires Apple Developer account for distribution

### Linux
- Builds .deb packages for Debian/Ubuntu
- Uses Docker container for reproducible builds
- Can be built on any platform with Docker

### Windows
- Builds both 64-bit (x86_64) and 32-bit (i686) installers
- Produces both .exe (Squirrel) and .msi installers
- Code signing requires Windows certificate for distribution

## Support

- **Issues**: https://github.com/vector-im/element-builder/issues
- **Element Desktop**: https://github.com/vector-im/element-desktop
- **Matrix Room**: #element-dev:matrix.org
