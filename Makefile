# A generic Makefile for a Java project using Maven

# Use the Maven wrapper if it exists, otherwise fall back to a system-wide mvn
MVN := $(if $(wildcard ./mvnw),./mvnw,mvn)
SHELL := /bin/bash

# Set log level
LOG_LEVEL := DEBUG

# Default target executed when 'make' is run without arguments
.DEFAULT_GOAL := help

# Phony targets don't represent files
.PHONY: help package run package-release test format format-check lint clean setup-hooks test-hooks

help: ## Show this help message
	@echo "Usage: make <target>"
	@echo ""
	@echo "Targets:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' Makefile | \
	awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-15s\033[0m %s\n", $$1, $$2}'

package: ## Compile and package the application into a JAR file
	@echo "Packaging application..."
	@$(MVN) -B package

run: package ## Build and run the packaged application
	@echo "Running application..."
	@java -Dlog4j.level=$(LOG_LEVEL) -jar target/uview.jar

package-release: ## Compile and package for a release
	@echo "Packaging project for release..."
	@$(MVN) -B package -P release

test: ## Run the tests and all other build checks
	@echo "Running tests..."
	@$(MVN) -B verify

format: ## Format Java source files
	@echo "Formatting source code..."
	@$(MVN) -B spotless:apply

format-check: ## Check code formatting without applying changes
	@echo "Checking code formatting..."
	@$(MVN) -B spotless:check

lint: ## Check code style
	@echo "Checking code style..."
	@$(MVN) -B checkstyle:check

clean: ## Remove all build artifacts
	@echo "Cleaning project..."
	@$(MVN) -B clean

setup-hooks: ## Install Git hooks (pre-commit and pre-push)
	@echo "Setting up Git hooks..."
	@if ! command -v pre-commit &> /dev/null; then \
	   echo "pre-commit not found. Please install it using 'pip install pre-commit'"; \
	   exit 1; \
	fi
	@pre-commit install --hook-type pre-commit
	@pre-commit install --hook-type pre-push
	@pre-commit install-hooks

test-hooks: ## Test Git hooks on all files
	@echo "Testing Git hooks..."
	@pre-commit run --all-files --show-diff-on-failure
