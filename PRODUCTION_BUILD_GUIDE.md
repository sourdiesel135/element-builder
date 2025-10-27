# Production Build Guide for Element Desktop

This guide explains how to set up and build production-ready Element Desktop applications for macOS, Linux, and Windows.

## Overview

Element Builder is designed to build Element Desktop (the Matrix chat client) for multiple platforms. The build system supports:

- **macOS** (universal binary: x86_64 + arm64)
- **Linux** (x86_64, .deb packages)
- **Windows** (x86_64 and i686/ia32)

## Build Types

### 1. Release Builds
Production releases from specific version tags/branches. Includes GPG verification of downloaded packages.

### 2. Nightly Builds
Automated daily builds from the `develop` branch.

## Prerequisites

### Required Infrastructure

#### 1. Build Machine
- **OS**: Linux, macOS, or Windows (Linux recommended for CI)
- **Node.js**: v16+ (tested with v22)
- **Yarn**: v1.22+
- **Git**: Latest version
- **Docker**: For Linux builds (when not on Linux)

#### 2. Windows Build Environment
For Windows target builds, you need:
- A Windows VM or physical machine
- VM management software (if using VMs)
- Environment variables:
  - `RIOTBUILD_WIN_VMNAME`: VM/machine name
  - `RIOTBUILD_WIN_USERNAME`: Windows username
  - `RIOTBUILD_WIN_PASSWORD`: Windows password

#### 3. Code Signing
- **Windows**: Code signing certificate and key container
  - Store in system keychain as `riot_key_container`
  - Format: `[{{passphrase}}]=container_name`
- **macOS**: Apple Developer certificate
- **Linux**: GPG keys for package signing

#### 4. Artifact Storage
Set at least one:
- **Rsync**: `RIOTBUILD_RSYNC_ROOT` - Path to rsync server
- **S3**: `RIOTBUILD_S3_BUCKET` and `RIOTBUILD_S3_ENDPOINT_URL`

#### 5. Release Builds Only
- **GPG Directory**: `./gnupg` containing Element's release public key
  ```bash
  mkdir gnupg
  curl -s https://packages.riot.im/element-release-key.asc | \
    gpg --homedir gnupg --import
  ```

### Optional
- **Matrix Logging**: Set these to log build status to a Matrix room:
  - `RIOTBUILD_BASEURL`: Matrix homeserver URL
  - `RIOTBUILD_ROOMID`: Room ID for logging
  - `RIOTBUILD_ACCESS_TOKEN`: Bot access token

## Installation

```bash
# Clone the repository
git clone https://github.com/vector-im/element-builder.git
cd element-builder

# Install dependencies
yarn install

# Build the TypeScript
yarn build
```

## Building

### Release Build

Build a specific version:

```bash
# Set required environment variables
export RIOTBUILD_WIN_VMNAME="windows-build-vm"
export RIOTBUILD_WIN_USERNAME="build"
export RIOTBUILD_WIN_PASSWORD="secure-password"
export RIOTBUILD_RSYNC_ROOT="user@server:/path/to/artifacts"

# Optional: Set custom targets (default builds all)
# Available: universal-apple-darwin, x86_64-unknown-linux-gnu,
#            x86_64-pc-windows-msvc, i686-pc-windows-msvc

# Build version v1.11.50
node lib/index.js --version v1.11.50

# Build with custom targets
node lib/index.js --version v1.11.50 \
  --targets universal-apple-darwin x86_64-unknown-linux-gnu

# Build with custom debian version
node lib/index.js --version v1.11.50 \
  --debian-version "1.11.50-1"
```

### Nightly Build

Runs continuously, building once per day at 8 AM:

```bash
# Set environment variables
export RIOTBUILD_WIN_VMNAME="windows-build-vm"
export RIOTBUILD_WIN_USERNAME="build"
export RIOTBUILD_WIN_PASSWORD="secure-password"
export RIOTBUILD_RSYNC_ROOT="user@server:/path/to/artifacts"
export BUILDKITE_API_KEY="your-buildkite-api-key"

# Start nightly builder (runs indefinitely)
node lib/index.js

# Force immediate build
node lib/index.js --force
```

## Simplified CI/CD Build

For CI/CD pipelines (GitHub Actions, GitLab CI, etc.), you can:

1. Build only for the current platform
2. Skip Windows VM requirement
3. Use Docker for cross-platform Linux builds

### GitHub Actions Example

```yaml
name: Build Element Desktop

on:
  push:
    tags:
      - 'v*'
  workflow_dispatch:

jobs:
  build-linux:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-node@v3
        with:
          node-version: '18'

      - name: Build
        run: |
          yarn install
          yarn build
          node lib/index.js --version ${{ github.ref_name }} \
            --targets x86_64-unknown-linux-gnu \
            --skip-sync

      - uses: actions/upload-artifact@v3
        with:
          name: linux-build
          path: packages.riot.im/

  build-macos:
    runs-on: macos-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-node@v3
        with:
          node-version: '18'

      - name: Build
        run: |
          yarn install
          yarn build
          node lib/index.js --version ${{ github.ref_name }} \
            --targets universal-apple-darwin \
            --skip-sync

      - uses: actions/upload-artifact@v3
        with:
          name: macos-build
          path: packages.riot.im/

  build-windows:
    runs-on: windows-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-node@v3
        with:
          node-version: '18'

      - name: Build
        run: |
          yarn install
          yarn build
          node lib/index.js --version ${{ github.ref_name }} `
            --targets x86_64-pc-windows-msvc `
            --skip-sync
        env:
          RIOTBUILD_WIN_VMNAME: localhost
          RIOTBUILD_WIN_USERNAME: runneradmin
          RIOTBUILD_WIN_PASSWORD: ""

      - uses: actions/upload-artifact@v3
        with:
          name: windows-build
          path: packages.riot.im/
```

## Output Artifacts

### Release Builds

Artifacts are organized in `packages.riot.im/desktop/`:

- **macOS**:
  - `install/macos/Element.dmg` - Installer
  - `update/macos/Element-{version}-mac.zip` - Update package
  - `update/macos/releases.json` - Update manifest

- **Linux**:
  - Debian packages in `debian/` repository structure

- **Windows**:
  - `install/win32/{arch}/Element Setup.exe` - Installer
  - `install/win32/{arch}/msi/Element Setup.msi` - MSI installer
  - `update/win32/{arch}/` - Update packages (.nupkg) and RELEASES file

### Nightly Builds

Same structure but in `packages.riot.im/nightly/` with "Nightly" suffix.

## Troubleshooting

### Issue: "No windows credentials set"
**Solution**: Set all three environment variables: `RIOTBUILD_WIN_VMNAME`, `RIOTBUILD_WIN_USERNAME`, `RIOTBUILD_WIN_PASSWORD`

### Issue: "Lock file found"
**Solution**: Another instance is running, or crashed. Remove `element-builder.lock` file.

### Issue: "No 'gnupg' directory found" (Release builds only)
**Solution**: Create the GPG directory with Element's public key:
```bash
mkdir gnupg
curl -s https://packages.riot.im/element-release-key.asc | gpg --homedir gnupg --import
```

### Issue: Docker not found (Linux builds on non-Linux)
**Solution**: Install Docker Desktop or use a Linux VM/CI runner.

### Issue: Build fails with network errors
**Solution**: Retry the build. Network errors during package downloads are common.

## Security Considerations

1. **Never commit secrets**: Use environment variables or secure secret management
2. **Code signing**: Properly secure your code signing certificates
3. **GPG keys**: Keep private keys secure, only import public keys for verification
4. **Access tokens**: Use read-only tokens where possible
5. **Windows credentials**: Use service accounts with minimal required permissions

## Advanced Configuration

### Custom Git Repository
```bash
node lib/index.js --version v1.11.50 \
  --git-repo https://github.com/your-fork/element-desktop.git
```

### Skip Artifact Sync
Build without uploading:
```bash
node lib/index.js --version v1.11.50 --skip-sync
```

### Sync Only
Upload previously built artifacts:
```bash
node lib/index.js --sync-only
```

## Migration to GitHub Actions

This project is marked as "No longer maintained" because Element Desktop's builds have been integrated into GitHub Actions. For the official Element Desktop builds, see:

https://github.com/vector-im/element-desktop/blob/develop/.github/workflows/build_and_deploy.yaml

However, this tool can still be useful for:
- Custom Element Desktop forks
- Internal/private deployments
- Testing build configurations
- Learning about Electron app distribution

## Support

- **Element Desktop**: https://github.com/vector-im/element-desktop
- **Element Builder**: https://github.com/vector-im/element-builder
- **Matrix Community**: #element-dev:matrix.org
