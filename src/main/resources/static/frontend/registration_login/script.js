// Switch between Login and Signup forms
document.querySelectorAll('.switch-btn').forEach(btn => 
    btn.addEventListener('click', () => {
        document.getElementById('a-container').classList.toggle('is-hidden');
        document.getElementById('b-container').classList.toggle('is-hidden');
    })
);

// Load business types for signup
window.addEventListener('DOMContentLoaded', () => {
    fetch('http://localhost:8080/user/business-types')
        .then(r => r.json())
        .then(d => {
            document.getElementById('businessType').innerHTML += 
                d.map(t => `<option value="${t}">${t.replace('_',' ')}</option>`).join('');
        })
        .catch(console.error);
});

const val = (f, sel) => f.querySelector(sel).value;

// 🔹 SIGNUP
document.getElementById("signup-form").addEventListener("submit", async e => {
    e.preventDefault();
    const f = e.target;
    try {
        const res = await fetch("http://localhost:8080/user/register", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                name: val(f, '#name'),
                email: val(f, '#email'),
                password: val(f, '#password'),
                businessName: val(f, '#businessName'),
                businessType: val(f, '#businessType')
            })
        });

        if (res.ok) {
            alert("Registration successful! You can now login.");
            // Switch to login form
            document.getElementById('a-container').classList.toggle('is-hidden');
            document.getElementById('b-container').classList.toggle('is-hidden');
        } else {
            alert("Registration failed!");
        }
    } catch {
        alert("Error registering user");
    }
});

// 🔹 LOGIN
document.getElementById("login-form").addEventListener("submit", async e => {
    e.preventDefault();

    const email = document.getElementById("loginEmail").value.trim();
    const password = document.getElementById("loginPassword").value.trim();

    if (!email || !password) {
        alert("Please enter both email and password.");
        return;
    }

    try {
        const response = await fetch("http://localhost:8080/user/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, password })
        });

        const token = await response.text(); // <- parse token as text

        if (response.ok && token) {
            localStorage.setItem("token", token); // save token
            window.location.href = "../owner_dashboard/dashboard.html"; // redirect
        } else {
            alert("Login failed. Please check your credentials.");
        }
    } catch (error) {
        console.error("Error:", error);
        alert("Error logging in. Check console for details.");
    }
});
