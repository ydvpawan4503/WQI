function fetchAnalytics() {
    // Extract ID from the URL (e.g., analytics.html?id=VILL-001)
    const urlParams = new URLSearchParams(window.location.search);
    const villageId = urlParams.get('id');

    if (!villageId) {
        document.getElementById('village-title').innerText = "Error: No Village Selected";
        return;
    }

    fetch(`/api/water/analytics/${villageId}`)
        .then(response => response.json())
        .then(data => {
            // Update Headers
            document.getElementById('village-title').innerText = `${data.villageName} (${data.villageId})`;
            
            // Update WQI Score
            const wqiElement = document.getElementById('card-wqi');
            wqiElement.innerText = data.currentWqi.toFixed(2);
            wqiElement.className = data.currentWqi > 100 ? "text-danger" : "text-success";

            // NEW: Update Last Updated Timestamp
            const lastUpdatedElement = document.getElementById('card-last-updated');
            if (data.date && data.time) {
                lastUpdatedElement.innerText = `${data.date} at ${data.time}`;
            } else {
                lastUpdatedElement.innerText = "Data Not Available";
            }

            // Draw Charts
            drawChart('phChart', 'pH Level', data.actualPh, data.standardPh, 14);
            drawChart('tdsChart', 'TDS (ppm)', data.actualTds, data.standardTds, Math.max(1000, data.actualTds + 200));
            drawChart('turbChart', 'Turbidity (NTU)', data.actualTurbidity, data.standardTurbidity, Math.max(5, data.actualTurbidity + 2));
        })
        .catch(error => {
            console.error("Error fetching analytics:", error);
            document.getElementById('village-title').innerText = "Error Loading Data";
        });
}

function drawChart(canvasId, label, actualValue, standardValue, yMax) {
    const ctx = document.getElementById(canvasId).getContext('2d');
    
    // Determine bar color (Red if actual exceeds standard limit)
    const barColor = actualValue > standardValue ? 'rgba(220, 53, 69, 0.8)' : 'rgba(25, 135, 84, 0.8)';

    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: ['Actual Reading', 'BIS 10500 Standard'],
            datasets: [{
                label: label,
                data: [actualValue, standardValue],
                backgroundColor: [barColor, 'rgba(13, 110, 253, 0.5)'],
                borderColor: [barColor.replace('0.8', '1'), 'rgba(13, 110, 253, 1)'],
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: true,
                    max: yMax
                }
            },
            plugins: {
                legend: { display: false } // Hide legend for cleaner look
            }
        }
    });
}

// Initial Load
window.onload = fetchAnalytics;