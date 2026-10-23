"use strict";

/* =========================================================
   CONFIGURATION
========================================================= */

const API_BASE = "/api";


/* =========================================================
   PAGE NAVIGATION
========================================================= */

function showSection(sectionId) {

    document
        .querySelectorAll(".page-section")
        .forEach(section => {

            section.classList.remove("active");

        });

    const section =
        document.getElementById(sectionId);

    if (section) {
        section.classList.add("active");
    }

    window.scrollTo({
        top: 0,
        behavior: "smooth"
    });

    if (sectionId === "cars") {
        loadCars();
    }

    if (sectionId === "bookings") {
        loadBookings();
        loadBookingCars();
    }
}


/* =========================================================
   API HELPER
========================================================= */

async function apiRequest(
    endpoint,
    options = {}
) {

    try {

        const response =
            await fetch(
                API_BASE + endpoint,
                {
                    headers: {
                        "Content-Type":
                            "application/json",
                        ...(options.headers || {})
                    },

                    ...options
                }
            );

        const data =
            await response.json();

        if (!response.ok) {

            throw new Error(
                data.message ||
                "Something went wrong."
            );
        }

        return data;

    } catch (error) {

        console.error(
            "API Error:",
            error
        );

        throw error;
    }
}


/* =========================================================
   DASHBOARD STATISTICS
========================================================= */

async function loadStatistics() {

    try {

        const data =
            await apiRequest(
                "/stats"
            );

        const stats =
            data.statistics;

        document.getElementById(
            "totalCars"
        ).textContent =
            stats.totalCars;

        document.getElementById(
            "availableCars"
        ).textContent =
            stats.availableCars;

        document.getElementById(
            "rentedCars"
        ).textContent =
            stats.rentedCars;

        document.getElementById(
            "totalBookings"
        ).textContent =
            stats.totalBookings;

    } catch (error) {

        console.error(
            "Could not load statistics:",
            error
        );
    }
}


/* =========================================================
   LOAD CARS
========================================================= */

async function loadCars() {

    const container =
        document.getElementById(
            "carsContainer"
        );

    if (!container) {
        return;
    }

    container.innerHTML =
        `<div class="loading">
            Loading cars...
        </div>`;

    try {

        const search =
            document.getElementById(
                "carSearch"
            ).value.trim();

        const category =
            document.getElementById(
                "categoryFilter"
            ).value;

        const params =
            new URLSearchParams();

        if (search) {
            params.append(
                "search",
                search
            );
        }

        if (category) {
            params.append(
                "category",
                category
            );
        }

        const query =
            params.toString()
                ? "?" + params.toString()
                : "";

        const data =
            await apiRequest(
                "/cars" + query
            );

        const cars =
            data.cars || [];

        renderCars(cars);

    } catch (error) {

        container.innerHTML =
            `<div class="empty-state">
                <div class="empty-state-icon">⚠️</div>
                <h3>Unable to load cars</h3>
                <p>${escapeHtml(error.message)}</p>
            </div>`;
    }
}


/* =========================================================
   RENDER CARS
========================================================= */

function renderCars(cars) {

    const container =
        document.getElementById(
            "carsContainer"
        );

    if (!cars.length) {

        container.innerHTML =
            `<div class="empty-state">
                <div class="empty-state-icon">🚗</div>
                <h3>No cars found</h3>
                <p>Try changing your search or category.</p>
            </div>`;

        return;
    }

    container.innerHTML =
        cars.map(car => {

            const availabilityClass =
                car.available
                    ? "available"
                    : "unavailable";

            const availabilityText =
                car.available
                    ? "● Available"
                    : "● Rented";

            return `
                <div class="car-card">

                    <div class="car-image">
                        🚘
                    </div>

                    <div class="car-content">

                        <div class="car-top">

                            <div>
                                <div class="car-name">
                                    ${escapeHtml(car.name)}
                                </div>

                                <div class="car-brand">
                                    ${escapeHtml(car.brand)}
                                    ${escapeHtml(car.model)}
                                </div>
                            </div>

                            <span class="category-badge">
                                ${escapeHtml(car.category)}
                            </span>

                        </div>


                        <div class="car-details">

                            <span class="detail">
                                👥 ${car.seats} seats
                            </span>

                            <span class="detail">
                                ⛽ ${escapeHtml(car.fuelType)}
                            </span>

                            <span class="detail">
                                ⚙️ ${escapeHtml(car.transmission)}
                            </span>

                        </div>


                        <div class="car-footer">

                            <div class="price">
                                ₹${formatNumber(car.pricePerDay)}
                                <span>/ day</span>
                            </div>

                            <div class="availability ${availabilityClass}">
                                ${availabilityText}
                            </div>

                        </div>

                    </div>

                </div>
            `;

        }).join("");
}


/* =========================================================
   LOAD CARS INTO BOOKING DROPDOWN
========================================================= */

async function loadBookingCars() {

    const select =
        document.getElementById(
            "bookingCar"
        );

    if (!select) {
        return;
    }

    try {

        const data =
            await apiRequest(
                "/cars"
            );

        const cars =
            data.cars || [];

        const availableCars =
            cars.filter(
                car => car.available
            );

        select.innerHTML =
            `<option value="">
                Select a car
            </option>`;

        availableCars.forEach(car => {

            const option =
                document.createElement(
                    "option"
                );

            option.value =
                car.id;

            option.textContent =
                `${car.name} - ₹${formatNumber(
                    car.pricePerDay
                )}/day`;

            select.appendChild(
                option
            );

        });

    } catch (error) {

        console.error(
            "Could not load booking cars:",
            error
        );
    }
}


/* =========================================================
   CREATE BOOKING
========================================================= */

async function createBooking(event) {

    event.preventDefault();

    const message =
        document.getElementById(
            "bookingMessage"
        );

    message.className =
        "form-message";

    message.textContent =
        "Creating booking...";


    const bookingData = {

        customerName:
            document.getElementById(
                "customerName"
            ).value.trim(),

        email:
            document.getElementById(
                "email"
            ).value.trim(),

        phone:
            document.getElementById(
                "phone"
            ).value.trim(),

        drivingLicenceNumber:
            document.getElementById(
                "drivingLicenceNumber"
            ).value.trim(),

        carId:
            document.getElementById(
                "bookingCar"
            ).value,

        startDate:
            document.getElementById(
                "startDate"
            ).value,

        endDate:
            document.getElementById(
                "endDate"
            ).value
    };


    if (
        new Date(bookingData.endDate)
        < new Date(bookingData.startDate)
    ) {

        message.className =
            "form-message error-message";

        message.textContent =
            "End date cannot be before start date.";

        return;
    }


    try {

        await apiRequest(
            "/bookings",
            {
                method: "POST",

                body:
                    JSON.stringify(
                        bookingData
                    )
            }
        );


        message.className =
            "form-message success-message";

        message.textContent =
            "Booking created successfully!";


        document
            .getElementById(
                "bookingForm"
            )
            .reset();


        showToast(
            "Booking created successfully."
        );


        await loadBookings();

        await loadBookingCars();

        await loadStatistics();

    } catch (error) {

        message.className =
            "form-message error-message";

        message.textContent =
            error.message;
    }
}


/* =========================================================
   LOAD BOOKINGS
========================================================= */

async function loadBookings() {

    const container =
        document.getElementById(
            "bookingsContainer"
        );

    if (!container) {
        return;
    }

    container.innerHTML =
        `<div class="loading">
            Loading bookings...
        </div>`;

    try {

        const data =
            await apiRequest(
                "/bookings"
            );

        const bookings =
            data.bookings || [];

        renderBookings(bookings);

    } catch (error) {

        container.innerHTML =
            `<div class="empty-state">
                <div class="empty-state-icon">⚠️</div>
                <h3>Unable to load bookings</h3>
                <p>${escapeHtml(error.message)}</p>
            </div>`;
    }
}


/* =========================================================
   RENDER BOOKINGS
========================================================= */

function renderBookings(bookings) {

    const container =
        document.getElementById(
            "bookingsContainer"
        );

    if (!bookings.length) {

        container.innerHTML =
            `<div class="empty-state">
                <div class="empty-state-icon">📋</div>
                <h3>No bookings yet</h3>
                <p>Create your first booking using the form above.</p>
            </div>`;

        return;
    }


    container.innerHTML =
        bookings.map(booking => {

            const status =
                booking.status || "ACTIVE";

            const statusClass =
                status === "CANCELLED"
                    ? "status-cancelled"
                    : status === "COMPLETED"
                        ? "status-completed"
                        : "status-active";


            const cancelButton =
                status === "CANCELLED"
                    ? ""
                    : `
                        <button
                            class="danger-button"
                            onclick="cancelBooking('${escapeHtml(
                                booking.id
                            )}')">

                            Cancel

                        </button>
                    `;


            return `
                <div class="booking-card">

                    <div class="booking-info">

                        <h3>
                            ${escapeHtml(
                                booking.customerName
                            )}
                        </h3>

                        <p>
                            ${escapeHtml(
                                booking.carName
                            )}
                            •
                            ${formatDate(
                                booking.startDate
                            )}
                            →
                            ${formatDate(
                                booking.endDate
                            )}
                        </p>

                        <p>
                            Booking ID:
                            ${escapeHtml(
                                booking.id
                            )}
                        </p>

                    </div>


                    <div class="booking-meta">

                        <div class="booking-meta-item">

                            <small>
                                DAYS
                            </small>

                            <strong>
                                ${booking.numberOfDays}
                            </strong>

                        </div>


                        <div class="booking-meta-item">

                            <small>
                                TOTAL
                            </small>

                            <strong>
                                ₹${formatNumber(
                                    booking.totalAmount
                                )}
                            </strong>

                        </div>


                        <span class="status-badge ${statusClass}">
                            ${escapeHtml(status)}
                        </span>


                        ${cancelButton}

                    </div>

                </div>
            `;

        }).join("");
}


/* =========================================================
   CANCEL BOOKING
========================================================= */

async function cancelBooking(
    bookingId
) {

    const confirmed =
        confirm(
            "Are you sure you want to cancel this booking?"
        );

    if (!confirmed) {
        return;
    }

    try {

        await apiRequest(
            "/bookings/" +
            encodeURIComponent(
                bookingId
            ),
            {
                method: "DELETE"
            }
        );

        showToast(
            "Booking cancelled."
        );

        await loadBookings();

        await loadBookingCars();

        await loadStatistics();

    } catch (error) {

        showToast(
            error.message
        );
    }
}


/* =========================================================
   TOAST
========================================================= */

function showToast(message) {

    const toast =
        document.getElementById(
            "toast"
        );

    const toastMessage =
        document.getElementById(
            "toastMessage"
        );

    toastMessage.textContent =
        message;

    toast.classList.add(
        "show"
    );

    setTimeout(() => {

        toast.classList.remove(
            "show"
        );

    }, 3000);
}


/* =========================================================
   HELPERS
========================================================= */

function formatNumber(value) {

    const number =
        Number(value);

    if (Number.isNaN(number)) {
        return "0";
    }

    return number.toLocaleString(
        "en-IN",
        {
            maximumFractionDigits: 2
        }
    );
}


function formatDate(dateString) {

    if (!dateString) {
        return "-";
    }

    const date =
        new Date(
            dateString + "T00:00:00"
        );

    return date.toLocaleDateString(
        "en-IN",
        {
            day: "2-digit",
            month: "short",
            year: "numeric"
        }
    );
}


function escapeHtml(value) {

    if (value === null
        || value === undefined) {

        return "";
    }

    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}


/* =========================================================
   INITIAL LOAD
========================================================= */

document.addEventListener(
    "DOMContentLoaded",
    async () => {

        /*
         * Set today's date as the minimum
         * booking date.
         */

        const today =
            new Date()
                .toISOString()
                .split("T")[0];

        const startDate =
            document.getElementById(
                "startDate"
            );

        const endDate =
            document.getElementById(
                "endDate"
            );

        if (startDate) {
            startDate.min = today;
        }

        if (endDate) {
            endDate.min = today;
        }


        /*
         * Load dashboard information.
         */

        await loadStatistics();

        await loadCars();

        await loadBookings();

        await loadBookingCars();

    }
);