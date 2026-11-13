# FitQueue demo

This walkthrough uses PowerShell and a locally running FitQueue instance.
Follow the setup instructions in the README before starting.

Run all commands in the same PowerShell terminal so session cookies
and variables remain available.

These accounts and passwords are for local testing only.
The example email addresses must not already be registered.

## 1. Register a demo account

Get a CSRF token and keep its session cookie:

```powershell
$baseUrl = "http://localhost:8080"

$csrf = Invoke-RestMethod -Uri "$baseUrl/api/csrf" -SessionVariable demoSession

$headers = @{}
$headers[$csrf.headerName] = $csrf.token
```

Register the member:

```powershell
$email = "demo@example.com"
$password = "demo-password-123"

$body = @{
    name = "Demo User"
    email = $email
    password = $password
} | ConvertTo-Json

$member = Invoke-RestMethod -Uri "$baseUrl/api/members" -Method Post -WebSession $demoSession -Headers $headers -ContentType "application/json" -Body $body

$member | ConvertTo-Json
```

The response contains the member's ID, name and email.
New accounts receive the MEMBER role.

## 2. Log in

Log in using the same session:

```powershell
Invoke-WebRequest -Uri "$baseUrl/login" -Method Post -WebSession $demoSession -Headers $headers -Body @{
    username = $email
    password = $password
} -UseBasicParsing
```

Confirm the authenticated account:

```powershell
Invoke-RestMethod -Uri "$baseUrl/api/members/me" -WebSession $demoSession | ConvertTo-Json
```

Get a fresh CSRF token after login:

```powershell
$csrf = Invoke-RestMethod -Uri "$baseUrl/api/csrf" -WebSession $demoSession

$headers = @{}
$headers[$csrf.headerName] = $csrf.token
```

## 3. Create a local demo administrator

Register a separate account:

```powershell
$adminEmail = "demo-admin@example.com"
$adminPassword = "demo-admin-password-123"

$adminBody = @{
    name = "Demo Admin"
    email = $adminEmail
    password = $adminPassword
} | ConvertTo-Json

$admin = Invoke-RestMethod -Uri "$baseUrl/api/members" -Method Post -WebSession $demoSession -Headers $headers -ContentType "application/json" -Body $adminBody
```

Promote this local demo account directly in PostgreSQL:

```powershell
docker compose exec db psql -U fitqueue -d fitqueue -c "UPDATE members SET role = 'ADMIN' WHERE email = 'demo-admin@example.com';"
```

Expected result: `UPDATE 1`.

Public registration always creates MEMBER accounts.
Administrator promotion is a manual local setup step.

## 4. Log in as the administrator

Create a separate session and obtain its CSRF token:

```powershell
$adminCsrf = Invoke-RestMethod -Uri "$baseUrl/api/csrf" -SessionVariable adminSession

$adminHeaders = @{}
$adminHeaders[$adminCsrf.headerName] = $adminCsrf.token
```

Log in:

```powershell
Invoke-WebRequest -Uri "$baseUrl/login" -Method Post -WebSession $adminSession -Headers $adminHeaders -Body @{
    username = $adminEmail
    password = $adminPassword
} -UseBasicParsing
```

Confirm the account and refresh the CSRF token:

```powershell
Invoke-RestMethod -Uri "$baseUrl/api/members/me" -WebSession $adminSession | ConvertTo-Json

$adminCsrf = Invoke-RestMethod -Uri "$baseUrl/api/csrf" -WebSession $adminSession

$adminHeaders = @{}
$adminHeaders[$adminCsrf.headerName] = $adminCsrf.token
```

Keep `demoSession` for member requests and `adminSession` for administrator requests.

## 5. Create a class as the administrator

Create a class with one seat, starting two days from now:

```powershell
$classBody = @{
    title = "Demo Yoga"
    capacity = 1
    startsAt = [DateTime]::UtcNow.AddDays(2).ToString("o")
} | ConvertTo-Json

$demoClass = Invoke-RestMethod -Uri "$baseUrl/api/classes" -Method Post -WebSession $adminSession -Headers $adminHeaders -ContentType "application/json" -Body $classBody

$demoClass | ConvertTo-Json
```

The following steps use `$demoClass.id` from the response.
No fixed database IDs are required.

## 6. Book the class as the member

Use the member session. No request body is needed:

```powershell
$booking = Invoke-RestMethod -Uri "$baseUrl/api/classes/$($demoClass.id)/bookings" -Method Post -WebSession $demoSession -Headers $headers

$booking | ConvertTo-Json
```

Expected status: `CONFIRMED`.
The member ID comes from the authenticated session.

## 7. Cancel the booking

Cancel the authenticated member's booking:

```powershell
$cancelledBooking = Invoke-RestMethod -Uri "$baseUrl/api/classes/$($demoClass.id)/bookings/me" -Method Delete -WebSession $demoSession -Headers $headers

$cancelledBooking | ConvertTo-Json
```

Expected status: `CANCELLED`, with the same booking ID.

Cancelled bookings remain stored. Repeating the cancellation returns the
cancelled booking; creating another booking for the same member and class
returns HTTP 409.