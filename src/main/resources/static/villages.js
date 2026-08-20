// villages.js
let currentPage = 0;
const pageSize = 10;

function fetchVillages(page) {
    currentPage = page;
    
    // Read Filter Values
    const keyword = document.getElementById('searchKeyword').value;
    const minWqi = document.getElementById('minWqi').value;
    const maxWqi = document.getElementById('maxWqi').value;
    const maxTds = document.getElementById('maxTds').value;

    // Build API Query URL
    let url = `/api/water/filter?page=${currentPage}&size=${pageSize}`;
    if (keyword) url += `&searchKeyword=${encodeURIComponent(keyword)}`;
    if (minWqi) url += `&minWqi=${minWqi}`;
    if (maxWqi) url += `&maxWqi=${maxWqi}`;
    if (maxTds) url += `&maxTds=${maxTds}`;

    fetch(url)
        .then(response => response.json())
        .then(data => {
            renderTable(data);
            document.getElementById('page-indicator').innerText = `Page ${currentPage + 1}`;
            
            // Disable Next button if returned rows are less than page size
            document.getElementById('btn-next').disabled = data.length < pageSize;
            document.getElementById('btn-prev').disabled = currentPage === 0;
        })
        .catch(error => console.error("Error fetching data:", error));
}

function renderTable(data) {
    const tbody = document.getElementById('village-table-body');
    tbody.innerHTML = '';

    data.forEach(v => {
        // Color code the WQI score badge
        let badgeClass = v.wqi > 100 ? 'status-critical' : 'status-safe';
        
        tbody.innerHTML += `
            <tr>
                <td><strong>${v.villageId}</strong></td>
                <td>${v.villageName}, ${v.district} (${v.state})</td>
                <td>${v.temperature}</td>
                <td>${v.ph}</td>
                <td>${v.tds}</td>
                <td>${v.turbidity}</td>
                <td><span class="${badgeClass}">${v.wqi.toFixed(2)}</span></td>
                <td>
                    <button class="btn btn-sm btn-info text-white" onclick="viewAnalytics('${v.villageId}')">View Details</button>
                </td>
            </tr>
        `;
    });
}

function changePage(direction) {
    fetchVillages(currentPage + direction);
}

function viewAnalytics(villageId) {
    // Redirect to the analytics page with the ID in the URL
    window.location.href = `analytics.html?id=${villageId}`;
}

// Initial load
window.onload = () => fetchVillages(0);