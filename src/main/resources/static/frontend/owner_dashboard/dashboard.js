// dashboard.js
window.addEventListener("DOMContentLoaded", async () => {
    // 1️⃣ Get token from localStorage
    const token = localStorage.getItem("token");
    if (!token) {
        // If no token, redirect to login
        window.location.href = "../registration_login/login.html";
        return;
    }

    const authHeader = token.startsWith("Bearer ") ? token : "Bearer " + token;
    console.log("Using token:", authHeader);
    let profile;

    // 2️⃣ Fetch user profile
    try {
        const profileRes = await fetch("http://localhost:8080/user/profile", {
            method: "GET",
            headers: {
                "Authorization": authHeader,
                "Content-Type": "application/json"
            },
            credentials: "include"
        });

        if (!profileRes.ok) throw new Error("Failed to fetch profile");

        profile = await profileRes.json();

        document.getElementById("ownerName").innerText = profile.name;
        document.getElementById("businessName").innerText = profile.businessName;

        console.log("Profile loaded:", profile);
    } catch (err) {
        console.error(err);
        alert("Error loading profile. Please login again.");
        localStorage.removeItem("token");
        window.location.href = "../registration_login/login.html";
        return;
    }

    // 3️⃣ Fetch analytics
    const fetchAnalytics = async (mode = "today", startDate = null, endDate = null) => {
        try {
            let url = `http://localhost:8080/analytics/dashboard?mode=${mode}`;
            if (mode === "range" && startDate && endDate) {
                url += `&startDate=${startDate}&endDate=${endDate}`;
            }

            const analyticsRes = await fetch(url, {
                headers: {
                    "Authorization": authHeader,
                    "Content-Type": "application/json"
                },
                credentials: "include"
            });

            if (!analyticsRes.ok) throw new Error("Failed to fetch analytics");

            const data = await analyticsRes.json();

            document.getElementById("totalItems").innerText = data.totalItems ?? 0;
            document.getElementById("lowStockItems").innerText = data.lowStock ?? 0;
            document.getElementById("outOfStockItems").innerText = data.outOfStock ?? 0;
            document.getElementById("recentlyAdded").innerText = data.recentlyAdded ?? 0;

            console.log("Analytics loaded:", data);
        } catch (err) {
            console.error(err);
            alert("Error loading analytics.");
        }
    };

    // Initial load
    fetchAnalytics("today");

    // 4️⃣ Analytics filter buttons
    const analyticsBtn = document.getElementById("analyticsBtn");
    analyticsBtn?.addEventListener("click", () => {
        const modeSelect = document.getElementById("analyticsMode")?.value || "today";
        const startDate = document.getElementById("startDate")?.value;
        const endDate = document.getElementById("endDate")?.value;

        if (modeSelect === "range" && (!startDate || !endDate)) {
            return alert("Please select both start and end dates for range mode");
        }

        fetchAnalytics(modeSelect, startDate, endDate);
    });

    // 5️⃣ Quick Search
    const searchBtn = document.getElementById("searchBtn");
    const searchInput = document.getElementById("searchInput");
    searchBtn?.addEventListener("click", async () => {
        const keyword = searchInput.value.trim();
        if (!keyword) return alert("Enter a keyword to search");

        try {
            const searchRes = await fetch(`http://localhost:8080/items/owner/${profile.id}/items/search?keyword=${keyword}`, {
                headers: {
                    "Authorization": authHeader,
                    "Content-Type": "application/json"
                },
                credentials: "include"
            });

            if (!searchRes.ok) throw new Error("Search failed");

            const items = await searchRes.json();

            const tableBody = document.getElementById("itemsTableBody");
            tableBody.innerHTML = "";

            items.forEach(item => {
                const tr = document.createElement("tr");
                tr.innerHTML = `
                    <td>${item.name}</td>
                    <td>${item.category ?? "-"}</td>
                    <td>${item.price}</td>
                    <td>${item.quantity}</td>
                    <td>${item.lowStockThreshold}</td>
                `;
                tableBody.appendChild(tr);
            });

            console.log("Search results:", items);
        } catch (err) {
            console.error(err);
            alert("Error searching items.");
        }
    });

    // 6️⃣ Logout
    const logoutBtn = document.getElementById("logoutBtn");
    logoutBtn?.addEventListener("click", () => {
        localStorage.removeItem("token");
        window.location.href = "../registration_login/login.html";
    });
});
