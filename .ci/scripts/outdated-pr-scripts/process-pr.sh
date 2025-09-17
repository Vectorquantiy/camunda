#!/bin/bash
# Process PRs in batch using our modular functions
# This script reads PR data from stdin (JSON) and processes each PR

set -euo pipefail

# Configuration
COMMIT_THRESHOLD="${COMMIT_THRESHOLD:-50}"
STALE_DAYS_THRESHOLD="${STALE_DAYS_THRESHOLD:-7}"

# Get the script directory and source our library functions
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/lib/check-pr-commits.sh"
source "$SCRIPT_DIR/lib/update-outdated-pr.sh"

# Check if running in dry-run mode
DRY_RUN="${DRY_RUN:-false}"
if [[ "$DRY_RUN" == "true" ]]; then
  echo "🔍 DRY-RUN MODE: Will calculate commits but not add labels/comments"
fi

DATE_PROGRAM=date
if command -v gdate >/dev/null; then
  DATE_PROGRAM=gdate
fi

# Read and process PRs from stdin
while IFS=' ' read -r pr_number branch_name base_branch repo_owner; do
  echo "🔍 Processing PR #$pr_number ($branch_name from $repo_owner targeting $base_branch)"
  
  commits_behind=$(check_pr_commits "$pr_number" "$branch_name" "$base_branch" "$repo_owner")
  
  # Check branch staleness
  latest_commit_date=$(git log origin/"$base_branch"..origin/"$branch_name" --pretty=format:"%ai" | head -1 2>/dev/null || echo "")
  latest_commit_timestamp=$("$DATE_PROGRAM" -d "$latest_commit_date" +%s 2>/dev/null || echo "0")
  current_timestamp=$("$DATE_PROGRAM" +%s)
  latest_age_days=$(( (current_timestamp - latest_commit_timestamp) / 86400 ))
  
  # Only call update function if PR is actually outdated
  reasons=()
  [[ "$commits_behind" -gt $COMMIT_THRESHOLD ]] && reasons+=("$commits_behind commits behind")
  [[ $latest_age_days -gt $STALE_DAYS_THRESHOLD ]] && reasons+=("$latest_age_days days stale")

  if [[ ${#reasons[@]} -gt 0 ]]; then
    reason=$(IFS=" and "; echo "${reasons[*]}")
    echo "🚨 PR #$pr_number is outdated: $reason" >&2
    update_outdated_pr "$pr_number" "$commits_behind" "$base_branch" "$COMMIT_THRESHOLD" "$latest_age_days" "$STALE_DAYS_THRESHOLD" "$DRY_RUN"
  else
    echo "✅ PR #$pr_number is up-to-date" >&2
  fi
  
  echo "---"
done < <(jq -r '.[] | "\(.pr_number) \(.branch_name) \(.base_branch) \(.repo_owner)"')
