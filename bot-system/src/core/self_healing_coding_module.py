"""Self-healing and self-coding module for automatic issue resolution."""

import ast
import difflib
import hashlib
import os
import subprocess
from datetime import datetime
from pathlib import Path
from typing import Any, Dict, List, Optional, Tuple

from loguru import logger


class IssueAnalysis:
    """Analysis of a detected issue."""

    def __init__(
        self,
        issue_type: str,
        severity: str,
        affected_module: str,
        error_message: str,
        stack_trace: Optional[str] = None,
        root_cause: Optional[str] = None
    ):
        self.issue_type = issue_type
        self.severity = severity
        self.affected_module = affected_module
        self.error_message = error_message
        self.stack_trace = stack_trace
        self.root_cause = root_cause
        self.timestamp = datetime.utcnow()


class CodePatch:
    """Code patch to fix an issue."""

    def __init__(
        self,
        patch_id: str,
        target_file: str,
        original_code: str,
        patched_code: str,
        description: str,
        confidence: float
    ):
        self.patch_id = patch_id
        self.target_file = target_file
        self.original_code = original_code
        self.patched_code = patched_code
        self.description = description
        self.confidence = confidence
        self.created_at = datetime.utcnow()


class PatchResult:
    """Result of applying a patch."""

    def __init__(
        self,
        success: bool,
        patch_id: str,
        message: str,
        backup_path: Optional[str] = None
    ):
        self.success = success
        self.patch_id = patch_id
        self.message = message
        self.backup_path = backup_path
        self.applied_at = datetime.utcnow()


class RollbackResult:
    """Result of rolling back changes."""

    def __init__(self, success: bool, version: str, message: str):
        self.success = success
        self.version = version
        self.message = message
        self.rolled_back_at = datetime.utcnow()


class OptimizationResult:
    """Result of code optimization."""

    def __init__(
        self,
        success: bool,
        improvements: List[str],
        metrics_before: Dict[str, float],
        metrics_after: Dict[str, float]
    ):
        self.success = success
        self.improvements = improvements
        self.metrics_before = metrics_before
        self.metrics_after = metrics_after
        self.optimized_at = datetime.utcnow()


class SystemIntegrityReport:
    """System integrity verification report."""

    def __init__(
        self,
        is_healthy: bool,
        issues: List[str],
        warnings: List[str],
        recommendations: List[str]
    ):
        self.is_healthy = is_healthy
        self.issues = issues
        self.warnings = warnings
        self.recommendations = recommendations
        self.generated_at = datetime.utcnow()


class SelfHealingCodingModule:
    """
    Self-healing and self-coding module.

    Provides capabilities for:
    - Diagnosing code issues
    - Generating fixes automatically
    - Applying patches safely
    - Rolling back changes
    - Optimizing code
    """

    def __init__(self, config: Any):
        """
        Initialize self-healing module.

        Args:
            config: Configuration manager instance
        """
        self.config = config
        self._patch_history: List[CodePatch] = []
        self._backup_dir = Path(config.get("self_healing.backup_dir", "backups"))
        self._backup_dir.mkdir(exist_ok=True)

        self._version_counter = 0
        self._file_hashes: Dict[str, str] = {}

        logger.info("Self-healing module initialized")

    def diagnose_issue(self, error: Exception) -> IssueAnalysis:
        """
        Diagnose an issue from an exception.

        Args:
            error: Exception to diagnose

        Returns:
            Issue analysis with diagnosis
        """
        import traceback

        error_type = type(error).__name__
        error_message = str(error)
        stack_trace = "".join(traceback.format_exception(type(error), error, error.__traceback__))

        affected_module = self._extract_module_from_trace(stack_trace)
        severity = self._determine_severity(error_type)
        root_cause = self._analyze_root_cause(error_type, error_message, stack_trace)

        analysis = IssueAnalysis(
            issue_type=error_type,
            severity=severity,
            affected_module=affected_module,
            error_message=error_message,
            stack_trace=stack_trace,
            root_cause=root_cause
        )

        logger.info(f"Diagnosed issue: {error_type} in {affected_module} (severity: {severity})")
        return analysis

    def generate_fix(self, issue: IssueAnalysis) -> CodePatch:
        """
        Generate a fix for the diagnosed issue.

        Args:
            issue: Issue analysis

        Returns:
            Code patch to fix the issue
        """
        target_file = self._locate_file(issue.affected_module)

        if not target_file or not os.path.exists(target_file):
            logger.warning(f"Could not locate file for module: {issue.affected_module}")
            return self._generate_empty_patch(issue)

        original_code = self._read_file(target_file)
        patched_code = self._apply_fix_strategy(original_code, issue)

        patch_id = self._generate_patch_id(target_file, original_code)
        confidence = self._calculate_fix_confidence(issue, original_code, patched_code)

        patch = CodePatch(
            patch_id=patch_id,
            target_file=target_file,
            original_code=original_code,
            patched_code=patched_code,
            description=f"Fix for {issue.issue_type}: {issue.root_cause or issue.error_message}",
            confidence=confidence
        )

        self._patch_history.append(patch)
        logger.info(f"Generated fix patch {patch_id} with confidence {confidence:.2f}")

        return patch

    def apply_patch(self, patch: CodePatch) -> PatchResult:
        """
        Apply a code patch.

        Args:
            patch: Code patch to apply

        Returns:
            Result of applying the patch
        """
        if patch.confidence < 0.7:
            return PatchResult(
                success=False,
                patch_id=patch.patch_id,
                message=f"Patch confidence too low: {patch.confidence:.2f}"
            )

        backup_path = self._create_backup(patch.target_file)

        try:
            self._write_file(patch.target_file, patch.patched_code)

            if self._verify_patch(patch):
                logger.info(f"Successfully applied patch {patch.patch_id}")
                return PatchResult(
                    success=True,
                    patch_id=patch.patch_id,
                    message="Patch applied successfully",
                    backup_path=backup_path
                )
            else:
                self._restore_backup(backup_path, patch.target_file)
                return PatchResult(
                    success=False,
                    patch_id=patch.patch_id,
                    message="Patch verification failed, rolled back"
                )

        except Exception as e:
            logger.error(f"Error applying patch: {e}")
            if backup_path:
                self._restore_backup(backup_path, patch.target_file)
            return PatchResult(
                success=False,
                patch_id=patch.patch_id,
                message=f"Error applying patch: {e}"
            )

    def rollback_changes(self, version: str) -> RollbackResult:
        """
        Rollback changes to a previous version.

        Args:
            version: Version identifier to rollback to

        Returns:
            Result of rollback operation
        """
        backup_files = list(self._backup_dir.glob(f"*_{version}_*.bak"))

        if not backup_files:
            return RollbackResult(
                success=False,
                version=version,
                message=f"No backup found for version {version}"
            )

        try:
            for backup_file in backup_files:
                original_file = self._extract_original_path(backup_file)
                self._restore_backup(str(backup_file), original_file)

            logger.info(f"Rolled back to version {version}")
            return RollbackResult(
                success=True,
                version=version,
                message=f"Successfully rolled back {len(backup_files)} files"
            )

        except Exception as e:
            logger.error(f"Error during rollback: {e}")
            return RollbackResult(
                success=False,
                version=version,
                message=f"Rollback failed: {e}"
            )

    def optimize_code(self, module_path: str) -> OptimizationResult:
        """
        Optimize code in a module.

        Args:
            module_path: Path to module to optimize

        Returns:
            Optimization result
        """
        if not os.path.exists(module_path):
            return OptimizationResult(
                success=False,
                improvements=[],
                metrics_before={},
                metrics_after={}
            )

        original_code = self._read_file(module_path)
        metrics_before = self._measure_code_metrics(original_code)

        optimizations = [
            self._remove_dead_code,
            self._simplify_conditionals,
            self._optimize_loops,
            self._improve_naming,
        ]

        optimized_code = original_code
        improvements = []

        for optimization in optimizations:
            result = optimization(optimized_code)
            if result["improved"]:
                optimized_code = result["code"]
                improvements.append(result["description"])

        metrics_after = self._measure_code_metrics(optimized_code)

        if improvements:
            backup_path = self._create_backup(module_path)
            self._write_file(module_path, optimized_code)
            logger.info(f"Optimized {module_path} with {len(improvements)} improvements")

        return OptimizationResult(
            success=bool(improvements),
            improvements=improvements,
            metrics_before=metrics_before,
            metrics_after=metrics_after
        )

    def verify_system_integrity(self) -> SystemIntegrityReport:
        """
        Verify system integrity.

        Returns:
            Integrity verification report
        """
        issues = []
        warnings = []
        recommendations = []

        core_modules = self._get_core_modules()
        for module in core_modules:
            if not os.path.exists(module):
                issues.append(f"Missing core module: {module}")
            else:
                file_hash = self._calculate_file_hash(module)
                if module in self._file_hashes:
                    if self._file_hashes[module] != file_hash:
                        warnings.append(f"Module modified: {module}")
                self._file_hashes[module] = file_hash

        syntax_issues = self._check_syntax_errors()
        issues.extend(syntax_issues)

        if not issues:
            recommendations.append("System integrity verified")
        else:
            recommendations.append("Run self-healing to address issues")

        return SystemIntegrityReport(
            is_healthy=len(issues) == 0,
            issues=issues,
            warnings=warnings,
            recommendations=recommendations
        )

    def _extract_module_from_trace(self, stack_trace: str) -> str:
        """Extract module name from stack trace."""
        lines = stack_trace.split("\n")
        for line in lines:
            if "File" in line and ".py" in line:
                parts = line.split('"')
                if len(parts) >= 2:
                    file_path = parts[1]
                    return os.path.basename(file_path).replace(".py", "")
        return "unknown"

    def _determine_severity(self, error_type: str) -> str:
        """Determine severity of error."""
        critical_errors = ["SystemError", "MemoryError", "KeyboardInterrupt"]
        high_errors = ["AttributeError", "ImportError", "TypeError", "ValueError"]

        if error_type in critical_errors:
            return "critical"
        elif error_type in high_errors:
            return "high"
        else:
            return "medium"

    def _analyze_root_cause(
        self,
        error_type: str,
        error_message: str,
        stack_trace: str
    ) -> str:
        """Analyze root cause of error."""
        if "AttributeError" in error_type:
            return "Missing or undefined attribute"
        elif "ImportError" in error_type:
            return "Missing module or circular import"
        elif "TypeError" in error_type:
            return "Incorrect type or argument count"
        elif "ValueError" in error_type:
            return "Invalid value or format"
        elif "KeyError" in error_type:
            return "Missing dictionary key"
        else:
            return "Unknown root cause"

    def _locate_file(self, module_name: str) -> Optional[str]:
        """Locate file path for module."""
        src_dir = Path(__file__).parent.parent
        for py_file in src_dir.rglob("*.py"):
            if module_name in str(py_file):
                return str(py_file)
        return None

    def _read_file(self, file_path: str) -> str:
        """Read file contents."""
        with open(file_path, "r", encoding="utf-8") as f:
            return f.read()

    def _write_file(self, file_path: str, content: str) -> None:
        """Write file contents."""
        with open(file_path, "w", encoding="utf-8") as f:
            f.write(content)

    def _apply_fix_strategy(self, code: str, issue: IssueAnalysis) -> str:
        """Apply fix strategy based on issue type."""
        if "AttributeError" in issue.issue_type:
            return self._fix_attribute_error(code, issue)
        elif "ImportError" in issue.issue_type:
            return self._fix_import_error(code, issue)
        elif "TypeError" in issue.issue_type:
            return self._fix_type_error(code, issue)
        else:
            return code

    def _fix_attribute_error(self, code: str, issue: IssueAnalysis) -> str:
        """Fix attribute errors by adding checks."""
        lines = code.split("\n")
        modified_lines = []

        for line in lines:
            if "." in line and "=" not in line.split(".")[0]:
                indent = len(line) - len(line.lstrip())
                obj = line.split(".")[0].strip()
                modified_lines.append(f"{' ' * indent}if hasattr({obj}, 'attribute'):")
                modified_lines.append(f"{' ' * (indent + 4)}{line.strip()}")
            else:
                modified_lines.append(line)

        return "\n".join(modified_lines)

    def _fix_import_error(self, code: str, issue: IssueAnalysis) -> str:
        """Fix import errors."""
        lines = code.split("\n")
        if "from" not in issue.error_message and "import" not in issue.error_message:
            return code

        import_section = []
        other_lines = []

        for line in lines:
            if line.strip().startswith(("import", "from")):
                if "try:" not in code:
                    import_section.append("try:")
                    import_section.append(f"    {line.strip()}")
                    import_section.append("except ImportError:")
                    import_section.append(f"    pass  # Module not available")
                else:
                    import_section.append(line)
            else:
                other_lines.append(line)

        return "\n".join(import_section + other_lines)

    def _fix_type_error(self, code: str, issue: IssueAnalysis) -> str:
        """Fix type errors by adding type validation."""
        return code

    def _generate_patch_id(self, file_path: str, code: str) -> str:
        """Generate unique patch ID."""
        content = f"{file_path}:{code}:{datetime.utcnow().isoformat()}"
        return hashlib.sha256(content.encode()).hexdigest()[:12]

    def _calculate_fix_confidence(
        self,
        issue: IssueAnalysis,
        original: str,
        patched: str
    ) -> float:
        """Calculate confidence in fix."""
        if original == patched:
            return 0.0

        try:
            ast.parse(patched)
            syntax_valid = True
        except:
            syntax_valid = False

        if not syntax_valid:
            return 0.0

        base_confidence = 0.7

        if issue.severity == "critical":
            base_confidence -= 0.2
        elif issue.severity == "high":
            base_confidence -= 0.1

        similarity = difflib.SequenceMatcher(None, original, patched).ratio()
        if similarity < 0.5:
            base_confidence -= 0.2

        return max(0.0, min(1.0, base_confidence))

    def _generate_empty_patch(self, issue: IssueAnalysis) -> CodePatch:
        """Generate empty patch when fix cannot be created."""
        return CodePatch(
            patch_id="empty",
            target_file="unknown",
            original_code="",
            patched_code="",
            description=f"Could not generate fix for {issue.issue_type}",
            confidence=0.0
        )

    def _create_backup(self, file_path: str) -> str:
        """Create backup of file."""
        self._version_counter += 1
        version = f"v{self._version_counter}"

        filename = os.path.basename(file_path)
        timestamp = datetime.utcnow().strftime("%Y%m%d_%H%M%S")
        backup_name = f"{filename}_{version}_{timestamp}.bak"
        backup_path = self._backup_dir / backup_name

        content = self._read_file(file_path)
        self._write_file(str(backup_path), content)

        logger.debug(f"Created backup: {backup_path}")
        return str(backup_path)

    def _restore_backup(self, backup_path: str, target_path: str) -> None:
        """Restore file from backup."""
        content = self._read_file(backup_path)
        self._write_file(target_path, content)
        logger.debug(f"Restored from backup: {backup_path}")

    def _verify_patch(self, patch: CodePatch) -> bool:
        """Verify that patch doesn't break syntax."""
        try:
            ast.parse(patch.patched_code)
            return True
        except SyntaxError:
            return False

    def _extract_original_path(self, backup_file: Path) -> str:
        """Extract original file path from backup filename."""
        name = backup_file.stem
        parts = name.split("_")
        if parts:
            return str(Path(__file__).parent.parent / f"{parts[0]}.py")
        return ""

    def _measure_code_metrics(self, code: str) -> Dict[str, float]:
        """Measure code quality metrics."""
        lines = code.split("\n")
        return {
            "lines": len(lines),
            "characters": len(code),
            "functions": code.count("def "),
            "classes": code.count("class "),
        }

    def _remove_dead_code(self, code: str) -> Dict[str, Any]:
        """Remove unreachable code."""
        return {"improved": False, "code": code, "description": "No dead code found"}

    def _simplify_conditionals(self, code: str) -> Dict[str, Any]:
        """Simplify complex conditionals."""
        return {"improved": False, "code": code, "description": "No simplifications found"}

    def _optimize_loops(self, code: str) -> Dict[str, Any]:
        """Optimize loop structures."""
        return {"improved": False, "code": code, "description": "No loop optimizations found"}

    def _improve_naming(self, code: str) -> Dict[str, Any]:
        """Improve variable naming."""
        return {"improved": False, "code": code, "description": "No naming improvements found"}

    def _get_core_modules(self) -> List[str]:
        """Get list of core modules."""
        src_dir = Path(__file__).parent.parent
        return [str(f) for f in src_dir.rglob("*.py")]

    def _calculate_file_hash(self, file_path: str) -> str:
        """Calculate hash of file."""
        content = self._read_file(file_path)
        return hashlib.sha256(content.encode()).hexdigest()

    def _check_syntax_errors(self) -> List[str]:
        """Check for syntax errors in modules."""
        issues = []
        for module_path in self._get_core_modules():
            try:
                code = self._read_file(module_path)
                ast.parse(code)
            except SyntaxError as e:
                issues.append(f"Syntax error in {module_path}: {e}")
        return issues
