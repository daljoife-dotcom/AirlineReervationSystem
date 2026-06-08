-- 1. جدول الطائرات (Aircraft) 
CREATE TABLE Aircraft (
    AircraftID INT IDENTITY(1,1) PRIMARY KEY,
    AircraftModel NVARCHAR(100) NOT NULL, -- مثل: Boeing 777
    TotalCapacity INT NOT NULL
);

-- 2. جدول المستخدمين 
CREATE TABLE SystemUsers (
    UserID INT IDENTITY(1,1) PRIMARY KEY,
    FullName NVARCHAR(150) NOT NULL,
    Email NVARCHAR(100) UNIQUE NOT NULL,
    PasswordHash NVARCHAR(255) NOT NULL,
    UserType NVARCHAR(20) NOT NULL
);

-- 3. جدول الرحلات المطور 
CREATE TABLE Flights (
    FlightID INT IDENTITY(1,1) PRIMARY KEY,
    FlightNumber NVARCHAR(20) UNIQUE NOT NULL,
    Destination NVARCHAR(100) NOT NULL,
    FlightDate DATETIME NOT NULL,
    Price DECIMAL(10, 2) NOT NULL,
    AircraftID INT NOT NULL, -- ربط الرحلة بطائرة معينة
    AvailableSeats INT NOT NULL,
    FOREIGN KEY (AircraftID) REFERENCES Aircraft(AircraftID)
);

-- 4. جدول الحجوزات 
CREATE TABLE Reservations (
    ReservationID INT IDENTITY(1,1) PRIMARY KEY,
    CustomerID INT NOT NULL,
    FlightID INT NOT NULL,
    BookingDate DATETIME DEFAULT GETDATE(),
    ReservationStatus NVARCHAR(30) DEFAULT 'Confirmed',
    FOREIGN KEY (CustomerID) REFERENCES SystemUsers(UserID) ON DELETE NO ACTION,
    FOREIGN KEY (FlightID) REFERENCES Flights(FlightID) ON DELETE CASCADE
);

-- 5. جدول عمليات الدفع 
CREATE TABLE Payments (
    PaymentID INT IDENTITY(1,1) PRIMARY KEY,
    ReservationID INT NOT NULL,
    Amount DECIMAL(10, 2) NOT NULL,
    PaymentDate DATETIME DEFAULT GETDATE(),
    PaymentMethod NVARCHAR(50) NOT NULL, -- (e.g., 'Credit Card', 'PayPal')
    FOREIGN KEY (ReservationID) REFERENCES Reservations(ReservationID) ON DELETE CASCADE
);