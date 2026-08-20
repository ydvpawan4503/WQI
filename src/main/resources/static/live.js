// live.js
let stompClient = null;

function renderTables(data) {
    if (!data) return;

    const bestBody = document.getElementById('best-table-body');
    const criticalBody = document.getElementById('critical-table-body');

    if (!bestBody || !criticalBody) {
        console.warn("Table elements not ready yet.");
        return;
    }

    // Clear previous rows
    bestBody.innerHTML = '';
    criticalBody.innerHTML = '';

    // Render Top 10 Best Villages
    if (Array.isArray(data.topBest) && data.topBest.length > 0) {
        data.topBest.forEach(v => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td><strong>${v.villageId || ''}</strong></td>
                <td>${v.villageName || ''}</td>
                <td><strong>${typeof v.wqi === 'number' ? v.wqi.toFixed(2) : v.wqi}</strong></td>
                <td><span class="status-safe">SAFE</span></td>
            `;
            bestBody.appendChild(row);
        });
    } else {
        bestBody.innerHTML = '<tr><td colspan="4" class="text-center text-muted">No records found</td></tr>';
    }

    // Render Top 10 Critical Villages
    if (Array.isArray(data.topCritical) && data.topCritical.length > 0) {
        data.topCritical.forEach(v => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td><strong>${v.villageId || ''}</strong></td>
                <td>${v.villageName || ''}</td>
                <td><strong>${typeof v.wqi === 'number' ? v.wqi.toFixed(2) : v.wqi}</strong></td>
                <td><span class="status-critical">CRITICAL</span></td>
            `;
            criticalBody.appendChild(row);
        });
    } else {
        criticalBody.innerHTML = '<tr><td colspan="4" class="text-center text-muted">No records found</td></tr>';
    }
}

// Immediate snapshot fetch upon loading the page
function fetchSnapshot() {
    fetch('/api/water/live')
        .then(response => {
            if (!response.ok) throw new Error('HTTP error ' + response.status);
            return response.json();
        })
        .then(data => {
            console.log("Snapshot received:", data);
            renderTables(data);
        })
        .catch(err => console.error("Snapshot fetch failed:", err));
}

// WebSocket live connection
function connectWebSocket() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.debug = null; // Clean console

    stompClient.connect({}, function () {
        console.log('Connected to WebSocket stream');
        stompClient.subscribe('/topic/waterquality', function (payload) {
            const data = JSON.parse(payload.body);
            renderTables(data);
        });
    }, function (err) {
        console.error("STOMP error:", err);
    });
}

// Wait until DOM is fully loaded before doing anything
document.addEventListener('DOMContentLoaded', () => {
    fetchSnapshot();
    connectWebSocket();
});