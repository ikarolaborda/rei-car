.PHONY: help up down build rebuild logs shell db-shell frontend dev seed clean

DOCKER_COMPOSE = docker-compose
APP_CONTAINER = reicar-app
DB_CONTAINER = reicar-mysql

help: ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

up: frontend ## Start all containers (builds frontend assets first)
	$(DOCKER_COMPOSE) up -d

down: ## Stop all containers
	$(DOCKER_COMPOSE) down

build: ## Build Docker images
	$(DOCKER_COMPOSE) build

rebuild: ## Rebuild Docker images without cache
	$(DOCKER_COMPOSE) build --no-cache

logs: ## Follow application logs
	$(DOCKER_COMPOSE) logs -f app

logs-all: ## Follow all container logs
	$(DOCKER_COMPOSE) logs -f

shell: ## Open shell in app container
	$(DOCKER_COMPOSE) exec app sh

db-shell: ## Open MySQL shell
	$(DOCKER_COMPOSE) exec db mysql -u reicar_user -preicar_pass_2024 reicar

frontend: ## Install and build frontend assets
	@echo "Installing frontend dependencies..."
	@cd reicar && npm install
	@echo "Frontend assets ready!"

dev: frontend seed ## Start development environment with seeded data
	$(DOCKER_COMPOSE) up -d
	@echo ""
	@echo "========================================"
	@echo "Development environment ready!"
	@echo "Application: http://localhost:38080"
	@echo "Database: localhost:33306"
	@echo "========================================"

seed: ## Seed database with real-world test data
	@echo "Seeding database with test data..."
	@$(DOCKER_COMPOSE) exec -T db mysql -u reicar_user -preicar_pass_2024 reicar < reicar/src/main/resources/db/seed/dev-data.sql 2>/dev/null || echo "Seed file not found or already seeded"

clean: ## Remove containers, volumes, and node_modules
	$(DOCKER_COMPOSE) down -v
	rm -rf reicar/node_modules
	rm -rf reicar/src/main/resources/static/css/bootstrap*.css
	rm -rf reicar/src/main/resources/static/css/fonts
	rm -rf reicar/src/main/resources/static/js/bootstrap*.js
	rm -rf reicar/src/main/resources/static/js/chart*.js

restart: ## Restart application container
	$(DOCKER_COMPOSE) restart app

status: ## Show container status
	$(DOCKER_COMPOSE) ps
