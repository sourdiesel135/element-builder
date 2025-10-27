# Element Builder

A production-ready build system for Element Desktop (Matrix chat client) supporting macOS, Linux, and Windows.

## Quick Start

```bash
# Install dependencies
yarn install && yarn build

# Quick build for your current platform
./scripts/local-build.sh

# Or build a specific version
./scripts/local-build.sh v1.11.50
```

📚 **Documentation:**
- **[Quick Start Guide](QUICKSTART.md)** - Get building in 5 minutes
- **[Production Build Guide](PRODUCTION_BUILD_GUIDE.md)** - Complete setup and configuration
- **GitHub Actions** - Automated multi-platform builds (see `.github/workflows/`)

## Features

- ✅ **Multi-platform builds**: macOS (Universal), Linux (x86_64), Windows (x86_64, i686)
- ✅ **Release & Nightly builds**: Production releases or automated daily builds
- ✅ **Code signing support**: Windows, macOS, and Linux package signing
- ✅ **Artifact management**: S3 and rsync deployment
- ✅ **CI/CD ready**: GitHub Actions workflow included
- ✅ **Simplified local builds**: Test builds without full infrastructure

## Supported Platforms

| Platform | Target ID | Output |
|----------|-----------|--------|
| macOS (Universal) | `universal-apple-darwin` | `.dmg` installer, `.zip` updates |
| Linux (x86_64) | `x86_64-unknown-linux-gnu` | `.deb` packages |
| Windows (64-bit) | `x86_64-pc-windows-msvc` | `.exe`, `.msi` installers |
| Windows (32-bit) | `i686-pc-windows-msvc` | `.exe`, `.msi` installers |

## Build Methods

### 1. Local Quick Build (Recommended for Testing)
Perfect for testing changes or building for your own use:
```bash
./scripts/local-build.sh
```

### 2. GitHub Actions (Recommended for Production)
Automated multi-platform builds with releases:
- Push a version tag: `git tag v1.0.0 && git push origin v1.0.0`
- Or trigger manually from Actions tab

### 3. Full Builder (Advanced)
Complete build system with all features:
```bash
node lib/index.js --version v1.11.50 --targets universal-apple-darwin x86_64-unknown-linux-gnu
```

## Requirements

**Minimal (for local builds):**
- Node.js 16+
- Yarn 1.22+
- Git

**Full production (multi-platform):**
- Docker (for Linux builds)
- Windows VM or machine (for Windows builds)
- Code signing certificates
- Artifact storage (S3 or rsync server)

See [PRODUCTION_BUILD_GUIDE.md](PRODUCTION_BUILD_GUIDE.md) for complete requirements.

## Project Status

**Note:** The official Element Desktop builds have been integrated into their main repository's GitHub Actions workflow. However, this builder remains useful for:

- Custom Element Desktop forks
- Internal/private deployments
- Testing build configurations
- Learning Electron app distribution
- Automated builds with custom infrastructure

Official Element Desktop: https://github.com/vector-im/element-desktop

## License

Apache License 2.0 - see [LICENSE](LICENSE)

## Support

- **Issues**: https://github.com/vector-im/element-builder/issues
- **Element Desktop**: https://github.com/vector-im/element-desktop
- **Matrix Community**: #element-dev:matrix.org
