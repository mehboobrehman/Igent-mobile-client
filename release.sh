#!/usr/bin/env bash

# release.sh - Automated build, version bump, git tag, and GitHub Release pipeline
set -euo pipefail

# Print step banner
info() {
    echo -e "\033[1;34m[INFO]\033[0m $1"
}

success() {
    echo -e "\033[1;32m[SUCCESS]\033[0m $1"
}

error() {
    echo -e "\033[1;31m[ERROR]\033[0m $1" >&2
}

# Default flags
BUMP_TYPE="patch"
SKIP_PUSH=false
DRY_RUN=false

# Parse arguments
while [[ $# -gt 0 ]]; do
    case "$1" in
        --major|major)
            BUMP_TYPE="major"
            shift
            ;;
        --minor|minor)
            BUMP_TYPE="minor"
            shift
            ;;
        --patch|patch)
            BUMP_TYPE="patch"
            shift
            ;;
        --skip-push)
            SKIP_PUSH=true
            shift
            ;;
        --dry-run)
            DRY_RUN=true
            shift
            ;;
        -h|--help)
            echo "Usage: ./release.sh [major|minor|patch] [--skip-push] [--dry-run]"
            echo ""
            echo "Options:"
            echo "  major|minor|patch  Version segment to increment (default: patch)"
            echo "  --skip-push        Skip pushing git commit/tag and creating GitHub release"
            echo "  --dry-run          Simulate build and release steps without modifying git/GH"
            exit 0
            ;;
        *)
            error "Unknown argument: $1"
            echo "Run ./release.sh --help for usage."
            exit 1
            ;;
    esac
done

# Locate Gradle executable
GRADLE_CMD="./gradlew"
if [ -f "./gradlew" ]; then
    GRADLE_CMD="./gradlew"
elif [ -f "./gradlew.bat" ]; then
    GRADLE_CMD="./gradlew.bat"
elif command -v gradlew &>/dev/null; then
    GRADLE_CMD="gradlew"
else
    error "Gradle wrapper executable not found!"
    exit 1
fi

# Locate GitHub CLI (gh) executable
GH_CMD=""
if command -v gh &>/dev/null; then
    GH_CMD="gh"
elif command -v gh.exe &>/dev/null; then
    GH_CMD="gh.exe"
elif [ -f "/c/Users/mehboob.rehman/Downloads/gh_2.102.0_windows_386/bin/gh.exe" ]; then
    GH_CMD="/c/Users/mehboob.rehman/Downloads/gh_2.102.0_windows_386/bin/gh.exe"
elif [ -f "C:/Users/mehboob.rehman/Downloads/gh_2.102.0_windows_386/bin/gh.exe" ]; then
    GH_CMD="C:/Users/mehboob.rehman/Downloads/gh_2.102.0_windows_386/bin/gh.exe"
elif [ -n "${GH_PATH:-}" ] && [ -f "$GH_PATH" ]; then
    GH_CMD="$GH_PATH"
else
    error "GitHub CLI (gh) not found in PATH or standard installation directory!"
    exit 1
fi

info "Using Gradle: $GRADLE_CMD"
info "Using GitHub CLI: $GH_CMD"

# 1. Read & Increment version in version.properties
PROPERTIES_FILE="version.properties"
if [ ! -f "$PROPERTIES_FILE" ]; then
    error "$PROPERTIES_FILE not found!"
    exit 1
fi

MAJOR=$(grep "^MAJOR=" "$PROPERTIES_FILE" | cut -d'=' -f2 | tr -d '\r')
MINOR=$(grep "^MINOR=" "$PROPERTIES_FILE" | cut -d'=' -f2 | tr -d '\r')
PATCH=$(grep "^PATCH=" "$PROPERTIES_FILE" | cut -d'=' -f2 | tr -d '\r')
BUILD=$(grep "^BUILD=" "$PROPERTIES_FILE" | cut -d'=' -f2 | tr -d '\r')

MAJOR=${MAJOR:-1}
MINOR=${MINOR:-0}
PATCH=${PATCH:-0}
BUILD=${BUILD:-0}

info "Current version: MAJOR=$MAJOR, MINOR=$MINOR, PATCH=$PATCH, BUILD=$BUILD (v${MAJOR}.${MINOR}.${PATCH})"

NEW_MAJOR=$MAJOR
NEW_MINOR=$MINOR
NEW_PATCH=$PATCH
NEW_BUILD=$((BUILD + 1))

case "$BUMP_TYPE" in
    major)
        NEW_MAJOR=$((MAJOR + 1))
        NEW_MINOR=0
        NEW_PATCH=0
        ;;
    minor)
        NEW_MINOR=$((MINOR + 1))
        NEW_PATCH=0
        ;;
    patch)
        NEW_PATCH=$((PATCH + 1))
        ;;
esac

VERSION_NAME="${NEW_MAJOR}.${NEW_MINOR}.${NEW_PATCH}"
TAG_NAME="v${VERSION_NAME}"

info "Bumping version ($BUMP_TYPE) -> ${VERSION_NAME} (BUILD=${NEW_BUILD}), tag: ${TAG_NAME}"

if [ "$DRY_RUN" = false ]; then
    cat <<EOF > "$PROPERTIES_FILE"
MAJOR=$NEW_MAJOR
MINOR=$NEW_MINOR
PATCH=$NEW_PATCH
BUILD=$NEW_BUILD
EOF
    success "Updated $PROPERTIES_FILE"
else
    info "[DRY-RUN] Would update $PROPERTIES_FILE to MAJOR=$NEW_MAJOR, MINOR=$NEW_MINOR, PATCH=$NEW_PATCH, BUILD=$NEW_BUILD"
fi

# 2. Build Debug and Release APKs
info "Building Debug and Release APKs using $GRADLE_CMD..."
$GRADLE_CMD assembleDebug assembleRelease

# Locate build outputs
DEBUG_APK=""
if [ -f "app/build/outputs/apk/debug/Igent-debug.apk" ]; then
    DEBUG_APK="app/build/outputs/apk/debug/Igent-debug.apk"
elif [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
    DEBUG_APK="app/build/outputs/apk/debug/app-debug.apk"
else
    error "Debug APK not found in app/build/outputs/apk/debug/"
    exit 1
fi

RELEASE_APK=""
if [ -f "app/build/outputs/apk/release/Igent-release.apk" ]; then
    RELEASE_APK="app/build/outputs/apk/release/Igent-release.apk"
elif [ -f "app/build/outputs/apk/release/app-release.apk" ]; then
    RELEASE_APK="app/build/outputs/apk/release/app-release.apk"
else
    error "Release APK not found in app/build/outputs/apk/release/"
    exit 1
fi

success "Debug APK generated: $DEBUG_APK"
success "Release APK generated: $RELEASE_APK"

if [ "$DRY_RUN" = true ]; then
    info "[DRY-RUN] Build completed successfully. Skipping git operations and GitHub release."
    exit 0
fi

# 3. Git operations: Stage, Commit, Tag
info "Performing Git operations..."
git add "$PROPERTIES_FILE"

# Stage any other modified tracked files if any
git add -u || true

COMMIT_MSG="chore(release): bump version to ${TAG_NAME}"
git commit -m "$COMMIT_MSG" || info "No changes to commit or commit already made."

if git rev-parse "$TAG_NAME" >/dev/null 2>&1; then
    info "Tag $TAG_NAME already exists locally."
else
    git tag -a "$TAG_NAME" -m "Release $TAG_NAME"
    success "Created Git tag: $TAG_NAME"
fi

# Push Git commit and tags
if [ "$SKIP_PUSH" = false ]; then
    CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)
    info "Pushing commit and tags to origin/$CURRENT_BRANCH..."
    git push origin "$CURRENT_BRANCH" --tags
    success "Pushed commit and tag $TAG_NAME to remote."

    # 4. GitHub Release Creation
    info "Creating GitHub Release $TAG_NAME and attaching APKs..."
    "$GH_CMD" release create "$TAG_NAME" "$DEBUG_APK" "$RELEASE_APK" \
        --title "Release $TAG_NAME" \
        --notes "Automated release $TAG_NAME" \
        --clobber

    success "GitHub Release $TAG_NAME successfully created with attached APKs!"
else
    info "[SKIP-PUSH] Skipping git push and GitHub release creation."
fi

success "End-to-end release process for $TAG_NAME completed successfully!"
