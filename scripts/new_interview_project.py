#!/usr/bin/env python3
"""
Bootstrap a fresh Spring Boot project from the basic-java-service template.

Given a project name (e.g. "Parking Lot") this script will:

  1. Clone the template repo at a chosen ref (branch/tag).
  2. Rename the Java package   com.app.<detected>  ->  com.app.<slug>
     (moves both main/ and test/ trees and rewrites every reference).
  3. Rename the Maven artifactId / name / description in pom.xml.
  4. Write the LLD problem statement to PROBLEM.md.
  5. Re-initialise git, commit, and (optionally) create + push a new
     GitHub repo named after the project.

Intended to be run right before an interview:

    python3 new_interview_project.py "Parking Lot" \
        --problem "Design a parking lot that supports ..."

    # just scaffold locally, don't touch GitHub:
    python3 new_interview_project.py "Rate Limiter" --no-push

Requirements:
  - git
  - gh (GitHub CLI), authenticated via `gh auth login`  (only for --push)

Only the Python standard library is used.
"""

import argparse
import re
import shutil
import subprocess
import sys
from pathlib import Path

# --- Defaults -------------------------------------------------------------
# Point these at YOUR finalised template. Use a branch/tag that already
# contains the validation, package rename, etc. (e.g. once merged to main).
DEFAULT_BASE_REPO = "git@github.com:engineergauravg/basic-java-service.git"
DEFAULT_BASE_REF = "main"

GROUP_PATH = "src/main/java/com/app"          # Maven groupId is com.app
TEXT_EXTS = {".java", ".xml", ".yml", ".yaml", ".properties", ".md"}
SKIP_DIRS = {".git", "target", ".idea", ".mvn"}


def run(cmd, cwd=None, check=True, capture=False):
    """Run a command, echoing it first."""
    print(f"  $ {' '.join(cmd)}")
    return subprocess.run(
        cmd, cwd=cwd, check=check,
        text=True,
        stdout=subprocess.PIPE if capture else None,
        stderr=subprocess.STDOUT if capture else None,
    )


def repo_slug(name: str) -> str:
    """'Parking Lot Service' -> 'parking-lot-service'."""
    s = re.sub(r"[^A-Za-z0-9]+", "-", name).strip("-").lower()
    return s or "interview-project"


def pkg_segment(name: str) -> str:
    """'Parking Lot' -> 'parkinglot' (a valid Java package segment)."""
    s = re.sub(r"[^A-Za-z0-9]+", "", name).lower()
    if not s:
        s = "app"
    if s[0].isdigit():
        s = "x" + s
    return s


def detect_old_segment(project: Path) -> str:
    """Find the single existing package directory under com/app."""
    group = project / GROUP_PATH
    if not group.is_dir():
        sys.exit(f"ERROR: {group} not found - is this the template repo?")
    dirs = [p.name for p in group.iterdir() if p.is_dir()]
    if len(dirs) != 1:
        sys.exit(f"ERROR: expected exactly one package under com/app, found: {dirs}")
    return dirs[0]


def rename_package(project: Path, old_seg: str, new_seg: str) -> None:
    if old_seg == new_seg:
        print(f"  package segment already '{new_seg}', skipping move")
        return
    old_pkg, new_pkg = f"com.app.{old_seg}", f"com.app.{new_seg}"

    # Move the package directory in both main and test trees.
    for base in ("src/main/java/com/app", "src/test/java/com/app"):
        src = project / base / old_seg
        if src.exists():
            src.rename(project / base / new_seg)

    # Rewrite every textual reference to the old package.
    changed = 0
    for path in project.rglob("*"):
        if not path.is_file() or path.suffix not in TEXT_EXTS:
            continue
        if any(part in SKIP_DIRS for part in path.parts):
            continue
        text = path.read_text(encoding="utf-8")
        if old_pkg in text:
            path.write_text(text.replace(old_pkg, new_pkg), encoding="utf-8")
            changed += 1
    print(f"  package {old_pkg} -> {new_pkg} ({changed} files updated)")


def update_pom(project: Path, slug: str, description: str) -> None:
    """Update only the project-level artifactId/name/description.

    We operate on the text after </parent> so the parent block and any XML
    comments are left untouched.
    """
    pom = project / "pom.xml"
    content = pom.read_text(encoding="utf-8")
    head, sep, tail = content.partition("</parent>")
    if not sep:
        sys.exit("ERROR: could not locate </parent> in pom.xml")

    tail = re.sub(r"<artifactId>.*?</artifactId>",
                  f"<artifactId>{slug}</artifactId>", tail, count=1, flags=re.S)
    tail = re.sub(r"<name>.*?</name>",
                  f"<name>{slug}</name>", tail, count=1, flags=re.S)
    tail = re.sub(r"<description>.*?</description>",
                  f"<description>{description}</description>", tail, count=1, flags=re.S)

    pom.write_text(head + sep + tail, encoding="utf-8")
    print(f"  pom.xml artifactId/name -> {slug}")


def write_problem(project: Path, name: str, problem: str) -> None:
    body = [f"# {name}\n"]
    body.append(problem.strip() + "\n" if problem else "_Paste the LLD problem statement here._\n")
    (project / "PROBLEM.md").write_text("\n".join(body), encoding="utf-8")
    print("  wrote PROBLEM.md")


def strip_bootstrap_script(project: Path) -> None:
    """Remove this tooling from the generated interview project."""
    script = project / "scripts" / "new_interview_project.py"
    if script.exists():
        script.unlink()
    scripts_dir = project / "scripts"
    if scripts_dir.is_dir() and not any(scripts_dir.iterdir()):
        scripts_dir.rmdir()


def find_gh():
    for cand in ("gh", "/opt/homebrew/bin/gh", "/usr/local/bin/gh"):
        if shutil.which(cand) or Path(cand).exists():
            return cand
    return None


def gh_authenticated(gh: str) -> bool:
    return run([gh, "auth", "status"], check=False, capture=True).returncode == 0


def main() -> None:
    ap = argparse.ArgumentParser(description="Scaffold a Spring Boot interview project from the template.")
    ap.add_argument("name", help='Project name, e.g. "Parking Lot"')
    ap.add_argument("--problem", default="", help="LLD problem statement (written to PROBLEM.md)")
    ap.add_argument("--base-repo", default=DEFAULT_BASE_REPO, help="Template repo URL or local path")
    ap.add_argument("--base-ref", default=DEFAULT_BASE_REF, help="Branch or tag to clone")
    ap.add_argument("--target-dir", default=None, help="Output directory (default: ./<slug>)")
    ap.add_argument("--public", action="store_true", help="Create a public repo (default: private)")
    ap.add_argument("--no-push", action="store_true", help="Scaffold locally; do not create/push a GitHub repo")
    ap.add_argument("--force", action="store_true", help="Overwrite target dir if it exists")
    ap.add_argument("--verify", action="store_true", help="Run `./mvnw test` after scaffolding")
    args = ap.parse_args()

    slug = repo_slug(args.name)
    seg = pkg_segment(args.name)
    target = Path(args.target_dir or slug).resolve()
    description = args.problem.strip().splitlines()[0] if args.problem.strip() else args.name

    print(f"Project name : {args.name}")
    print(f"Repo slug    : {slug}")
    print(f"Java package : com.app.{seg}")
    print(f"Target dir   : {target}\n")

    if target.exists():
        if not args.force:
            sys.exit(f"ERROR: {target} already exists (use --force to overwrite)")
        shutil.rmtree(target)

    print("==> Cloning template")
    run(["git", "clone", "--depth", "1", "--branch", args.base_ref, args.base_repo, str(target)])
    shutil.rmtree(target / ".git")

    print("==> Renaming package")
    old_seg = detect_old_segment(target)
    rename_package(target, old_seg, seg)

    print("==> Updating pom.xml")
    update_pom(target, slug, description)

    print("==> Writing problem statement")
    write_problem(target, args.name, args.problem)
    strip_bootstrap_script(target)

    print("==> Initialising git")
    run(["git", "init", "-b", "main"], cwd=target)
    run(["git", "add", "."], cwd=target)
    run(["git", "commit", "-q", "-m", f"Bootstrap {args.name} from basic-java-service template"], cwd=target)

    if args.verify:
        print("==> Verifying build (./mvnw test)")
        run(["./mvnw", "test"], cwd=target, check=False)

    if args.no_push:
        print(f"\nDone (local only). Project at: {target}")
        return

    gh = find_gh()
    if gh is None:
        print("\nWARNING: gh CLI not found - skipping GitHub repo creation.")
        print(f"Project is ready locally at: {target}")
        print(f"To publish:  cd {target} && git remote add origin <url> && git push -u origin main")
        return
    if not gh_authenticated(gh):
        print("\nWARNING: gh is not authenticated - skipping GitHub repo creation.")
        print("Run `gh auth login` then publish with:")
        print(f"  cd {target} && {gh} repo create {slug} {'--public' if args.public else '--private'} --source . --remote origin --push")
        return

    print("==> Creating GitHub repo and pushing")
    visibility = "--public" if args.public else "--private"
    run([gh, "repo", "create", slug, visibility, "--source", ".", "--remote", "origin", "--push"], cwd=target)
    print(f"\nDone. New repo '{slug}' created and pushed. Local copy: {target}")


if __name__ == "__main__":
    main()
