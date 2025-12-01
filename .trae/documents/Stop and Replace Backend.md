## Goal
Stop the currently running API backend and replace it with the updated backend code, then rebuild and validate the service.

## Assumptions
- Backend is the .NET 8 API in `backend/JNBFitness/JNBFitness` (entrypoint `Program.cs`).
- Replacement source is `backend/JNBFitness/JNBFitness.zip` found in the repo.
- MySQL connection and JWT secrets are managed via `appsettings.json` and environment variables.

## Stop Current Backend
1. Identify any running API process (`JNBFitness.exe`) and terminate it gracefully.
2. Ensure no local dev server is bound to the API ports before proceeding.

## Backup
1. Create a timestamped backup of `backend/JNBFitness/JNBFitness` and `JNBFitness.sln`.
2. Preserve `appsettings.json` and `appsettings.Development.json` (DB connection, JWT keys).

## Replace Code
1. Extract `JNBFitness.zip` into `backend/JNBFitness/` overwriting project files.
2. Keep existing `appsettings.*.json` unless the zip explicitly includes updated config.
3. Verify solution/project references (`JNBFitness.sln`, `JNBFitness.csproj`).

## Dependencies
1. Restore NuGet packages for the solution.
2. Confirm EF Core and Pomelo MySQL provider versions match target (`Program.cs` shows MySQL via Pomelo). 

## Configuration
1. Validate `ConnectionStrings:DefaultConnection` in `appsettings.json` and dev file.
2. Validate JWT config: `Jwt:Key`, `Jwt:Issuer`, `Jwt:Audience` referenced in `Program.cs`.
3. Confirm CORS policy aligns with frontend needs (`AllowAnyOrigin` currently).

## Build & Run
1. Build solution and fix any compile issues.
2. Run API and ensure Swagger is available at `/swagger`.
3. Confirm startup banner and URLs are logged (see `Program.cs`).

## Validate Endpoints
- Smoke test key routes:
  - `POST /api/Auth/login` and `register`
  - `GET /api/Coachs`, `GET /api/CoursCollectifs`, `GET /api/Articles`
- Verify repository injection remains intact (e.g., `IEcritureLedgerRepository` registration in DI).

## Database
1. Ensure migrations are up-to-date; apply pending migrations if the replacement includes schema changes.
2. Run a health check against MySQL connection.

## Rollback Plan
- If issues arise, restore the backup directory and configs, rebuild, and rerun.

## Confirmation
If this plan matches your intent (“stop and replace backend from the zip, then rebuild and verify”), confirm and I’ll execute it end-to-end. If you prefer a different replacement source or scope (e.g., only swapping specific modules), I’ll adapt accordingly.