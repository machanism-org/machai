/**
 * Project scanning, guidance extraction, and prompt orchestration for the Ghostwriter command-line
 * tool.
 *
 * <p>
 * The {@code org.machanism.machai.gw.processor} package contains the CLI entry point and the
 * processors responsible for scanning a project directory tree, extracting embedded
 * {@code @guidance:} directives, and invoking the configured
 * {@link org.machanism.machai.ai.manager.Genai GenAI provider}.
 * </p>
 *
 * <h2>Key components</h2>
 * <ul>
 *   <li>
 *     {@link org.machanism.machai.gw.processor.Ghostwriter}: CLI entry point that reads
 *     configuration and launches scans.
 *   </li>
 *   <li>
 *     {@link org.machanism.machai.gw.processor.GuidanceProcessor}: filesystem scanner that extracts
 *     per-file guidance via {@link org.machanism.machai.gw.reviewer.Reviewer reviewers} and
 *     processes multi-module projects child-first.
 *   </li>
 *   <li>
 *     {@link org.machanism.machai.gw.processor.ActProcessor}: executes predefined TOML-based prompt
 *     templates ("acts") against matching files.
 *   </li>
 *   <li>
 *     {@link org.machanism.machai.gw.processor.AIFileProcessor}: prompt composition and provider
 *     invocation (instructions, project metadata, and optional input logging).
 *   </li>
 *   <li>
 *     {@link org.machanism.machai.gw.processor.AbstractFileProcessor}: shared traversal utilities,
 *     include/exclude matching, and module handling.
 *   </li>
 * </ul>
 *
 * <h2>Processing model</h2>
 * <p>
 * Processing is strictly filesystem-based: projects are not built and dependencies are not
 * resolved during scanning. When a path matcher is used (via {@code glob:} or {@code regex:}),
 * inclusion decisions are made against project-relative paths and known build/tooling directories
 * are ignored.
 * </p>
 */
package org.machanism.machai.gw.processor;
