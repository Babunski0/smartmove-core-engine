# SmartMove Core Engine

A Java-based core engine for the SmartMove application with integrated SonarQube code quality analysis.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [SonarQube Setup](#sonarqube-setup)
- [Running the Application](#running-the-application)
- [Running Code Quality Analysis](#running-code-quality-analysis)
- [Project Structure](#project-structure)
- [Contributing](#contributing)

## Prerequisites

Before you begin, ensure you have the following installed:

- **Java 17** or higher ([Download here](https://adoptium.net/))
- **Maven 3.6+** ([Download here](https://maven.apache.org/download.cgi))
- **Git** ([Download here](https://git-scm.com/downloads))
- **Docker Desktop** ([Download here](https://www.docker.com/products/docker-desktop/)) - for SonarQube

### Verify Installation
```bash
java -version      # Should show Java 17 or higher
mvn -version       # Should show Maven 3.6 or higher
git --version      # Should show Git version
docker --version   # Should show Docker version
```

## Installation

### 1. Clone the Repository
```bash
git clone https://github.com/your-username/smartmove-core-engine.git
cd smartmove-core-engine
```

### 2. Configure Git User (if using multiple accounts)

Set your Git user for this repository:
```bash
git config user.name "Your Name"
git config user.email "your.email@example.com"
```

### 3. Setup Environment Variables
```bash
# Copy the template
cp .env.example .env

# Edit and add your SonarQube token (see SonarQube Setup section)
# Your .env file is in .gitignore and never committed to Git
```

### 4. Build the Project
```bash
mvn clean install
```

## Running the Application

### Start the Application
```bash
mvn spring-boot:run
```

The application will start on: **http://localhost:8080/smartmove**

### Test the Health Endpoint
```bash
curl http://localhost:8080/smartmove/actuator/health
# Response: {"status":"UP"}
```

### Build and Package
```bash
mvn clean package
# JAR file: target/smartmove-core-1.0-SNAPSHOT.jar
```

### Run Tests
```bash
mvn clean test
```

## SonarQube Setup

### 1. Start SonarQube in Docker
```bash
# Pull the SonarQube Community image
docker pull sonarqube:community

# Run SonarQube container
docker run -d --name sonarqube-community -p 9000:9000 sonarqube:community
```

Wait 2-3 minutes for SonarQube to start, then access it at: **http://localhost:9000**

**Default credentials:**
- Username: `admin`
- Password: `admin`

(You'll be prompted to change the password on first login)

### 2. Generate SonarQube Token

1. Log in to SonarQube at http://localhost:9000
2. Go to **Projects** → **SmartMove Core Engine**
3. Click **Project Settings** (top right dropdown)
4. Navigate to **Analysis Tokens** in the left sidebar
5. Click **Generate**:
    - **Name:** `team-analysis-token`
    - Click **Generate**
    - **Copy the token** (you won't see it again!)

### 3. Configure Environment Variables

1. **Copy the template file:**
```bash
   cp .env.example .env
```

2. **Edit the `.env` file** and replace `your_sonarqube_token_here` with the actual token you generated in step 2.

3. **Important:** The `.env` file is already in `.gitignore` and will never be committed to Git. This keeps your token secure.

## Running Code Quality Analysis

**Windows (CMD):**
```cmd
# Load the token from .env file
for /f "tokens=1,2 delims==" %a in (.env) do set %a=%b

# Run analysis
mvn clean verify sonar:sonar -Dsonar.login=%SONAR_TOKEN%
```

**Windows (PowerShell):**
```powershell
# Load the token from .env file
Get-Content .env | ForEach-Object {
    if ($_ -match '^([^=]+)=(.*)$') {
        [System.Environment]::SetEnvironmentVariable($matches[1], $matches[2])
    }
}

# Run analysis
mvn clean verify sonar:sonar -Dsonar.login=$env:SONAR_TOKEN
```

**Linux/Mac:**
```bash
# Load the token from .env file
export $(cat .env | xargs)

# Run analysis
mvn clean verify sonar:sonar -Dsonar.login=$SONAR_TOKEN
```

### View Results

After the analysis completes, view the results at:
**http://localhost:9000/dashboard?id=smartmove-core-engine**

## Project Structure
```
smartmove-core-engine/
├── src/
│   ├── main/
│   │   ├── java/com/smartmove/
│   │   │   ├── App.java                 # Spring Boot application
│   │   │   ├── domain/                  # Enums, entities, rules
│   │   │   ├── dto/                     # Request/response DTOs
│   │   │   ├── service/                 # Business logic
│   │   │   ├── controller/              # REST endpoints
│   │   │   ├── exception/               # Custom exceptions
│   │   │   └── config/                  # Spring configuration
│   │   └── resources/
│   │       └── application.properties   # Configuration
│   └── test/
│       └── java/com/smartmove/          # Unit tests
├── target/                              # Compiled JAR (generated)
├── .env                                 # Environment variables (NOT in Git)
├── .env.example                         # Environment template
├── .gitignore                           # Git ignore rules
├── pom.xml                              # Maven configuration
└── README.md                            # This file
```

## Key Technologies

- **Java 17** - Programming language
- **Spring Boot 3.1.11** - REST API framework
- **JUnit 5** - Testing framework
- **Mockito** - Mocking library
- **Lombok** - Reduces boilerplate code
- **JaCoCo** - Code coverage
- **SonarQube** - Code quality and security

## Contributing

### Branch Naming Convention

- `main` - Production-ready code
- `feature/*` - New features (e.g., `feature/user-authentication`)
- `bugfix/*` - Bug fixes (e.g., `bugfix/login-error`)
- `hotfix/*` - Urgent production fixes

### Workflow

1. Create a feature branch:
```bash
   git checkout -b feature/your-feature-name
```

2. Make your changes and commit:
```bash
   git add .
   git commit -m "Add: description of your changes"
```

3. Run tests and quality checks:
```bash
   mvn clean verify sonar:sonar -Dsonar.login=$SONAR_TOKEN
```

4. Push your branch:
```bash
   git push origin feature/your-feature-name
```

5. Create a Pull Request on GitHub

### Code Quality Standards

- **All tests must pass** before merging
- **Code coverage should be above 50%** (visible in SonarQube)
- **No critical or blocker issues** in SonarQube
- **Follow Java coding conventions**

## Troubleshooting

### Application Won't Start
```bash
# Check if port 8080 is in use
lsof -i :8080

# Kill process if needed (macOS/Linux)
kill -9 <PID>

# Try building again
mvn clean install
mvn spring-boot:run
```

### SonarQube Container Not Running
```bash
# Check if container is running
docker ps

# Start the container if stopped
docker start sonarqube-community

# View logs if there are issues
docker logs sonarqube-community
```

### Maven Build Fails
```bash
# Clean and rebuild
mvn clean install -U

# Skip tests if needed (not recommended)
mvn clean install -DskipTests
```

### SonarQube Analysis Fails

1. **Check SonarQube is running:** Open http://localhost:9000
2. **Verify token:** Check your `.env` file has the correct token
3. **Check project exists:** The project should exist in SonarQube
4. **Check logs:** Look for error messages in the Maven output

### Docker Permission Issues on Windows

Run the `git config --global --add safe.directory` command as shown in any Git ownership errors.
