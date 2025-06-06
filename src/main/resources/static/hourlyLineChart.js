/**
 * Fetches data from the API and renders a line chart.
 * @param {Object} config - Cấu hình biểu đồ.
 * @param {string} config.canvasId - ID của phần tử canvas (ví dụ: 'hourlyLineChart').
 * @param {string} config.apiUrl - Đường dẫn API để lấy dữ liệu.
 * @param {string} config.chartLabel - Nhãn của dataset (ví dụ: "Số lượt đăng ký theo giờ").
 * @param {string} config.xAxisLabel - Nhãn trục X (ví dụ: "Giờ").
 * @param {string} config.yAxisLabel - Nhãn trục Y (ví dụ: "Số lượt").
 */
// khai báo hàm bất đồng bộ để lấy dữ liệu và vẽ biểu đồ
async function renderLineChart(config) {
    const { canvasId, apiUrl, chartLabel, xAxisLabel, yAxisLabel, processTimeLabels = false, reverseData = false } = config;
    const canvasElement = document.getElementById(canvasId);// lấy phần tử canvas bằng ID
    const errorDiv = document.getElementById('chartErrorMessages');

    if (!canvasElement) {
        console.error(`Lỗi: Không tìm thấy canvas với ID "${canvasId}".`);
        if (errorDiv) errorDiv.innerHTML += `<p>Lỗi cấu hình: Canvas '${canvasId}' không tồn tại.</p>`;
        return;
    }

    // Lấy context vẽ 2D của canvas
    const ctx = canvasElement.getContext('2d');

    try {
        console.log(`Đang fetch dữ liệu cho ${canvasId} từ ${apiUrl}`);// In ra log để theo dõi quá trình fetch dữ liệu
        const response = await fetch(apiUrl);// Gọi API để lấy dữ liệu biểu đồ

        if (!response.ok) {
            let errorMessage = `Lỗi API (${apiUrl}) cho ${canvasId}: ${response.status} ${response.statusText}`;
            try {
                const errorData = await response.json();
                errorMessage += ` - ${errorData.body || errorData.message || JSON.stringify(errorData)}`;
            } catch (e) { }
            console.error(errorMessage);
            ctx.clearRect(0, 0, canvasElement.width, canvasElement.height);
            ctx.font = "16px Arial";
            ctx.fillStyle = "red";
            ctx.fillText(`Lỗi tải dữ liệu cho biểu đồ ${chartLabel.toLowerCase()}.`, 10, 50);
            if (errorDiv) errorDiv.innerHTML += `<p>Lỗi biểu đồ (${canvasId}): ${errorMessage}</p>`;
            return;
        }

        const chartServerData = await response.json();// nếu phản hồi thành công, chuyển đổi dữ liệu JSON từ API
        if (processTimeLabels) { // Chỉ xử lý nếu cờ là true
            if (chartServerData.labels && Array.isArray(chartServerData.labels)) {
                chartServerData.labels = chartServerData.labels.map(datetime => {
                    if (typeof datetime === 'string' && datetime.includes(" ")) {
                        return datetime.split(" ")[1]; // Giữ phần giờ, bỏ ngày
                    }
                    return datetime; // Trả lại nguyên gốc nếu không phải định dạng mong muốn
                });
            }
        }
        if (reverseData) {
            chartServerData.labels.reverse();// Đảo ngược thứ tự giờ
            chartServerData.data.reverse();// Đảo ngược số lượt đăng ký
        }


        if (!chartServerData || !chartServerData.labels || !chartServerData.data) {
            // Nếu dữ liệu không đúng định dạng, hiển thị thông báo lỗi
            const errorMsg = `Lỗi: Dữ liệu từ API ${apiUrl} cho ${canvasId} không đúng định dạng (thiếu "labels" hoặc "data").`;
            console.error(errorMsg, chartServerData);
            ctx.clearRect(0, 0, canvasElement.width, canvasElement.height);
            ctx.font = "16px Arial";
            ctx.fillStyle = "orange";
            ctx.fillText(`Dữ liệu biểu đồ ${chartLabel.toLowerCase()} không hợp lệ.`, 10, 50);
            if (errorDiv) errorDiv.innerHTML += `<p>${errorMsg}</p>`;
            return;
        }

        if (canvasElement.chartInstance) {
            canvasElement.chartInstance.destroy();// Nếu biểu đồ đã tồn tại, hủy nó trước khi tạo mới
        }

        // Tạo biểu đồ mới
        canvasElement.chartInstance = new Chart(ctx, {
            type: 'line',
            data: {
                labels: chartServerData.labels,
                datasets: [{
                    label: chartLabel,
                    data: chartServerData.data,
                    tension: 0.1,
                    backgroundColor: 'rgba(123, 215, 246, 0.6)',
                    borderColor: 'rgb(108, 207, 235)',
                    borderWidth: 3,
                    pointRadius: 4,
                    fill: true
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: { beginAtZero: true, title: { display: true, text: yAxisLabel } },
                    x: { title: { display: true, text: xAxisLabel } }
                },
                plugins: { legend: { display: true, position: 'top' } }
            }
        });

    } catch (error) {
        console.error(`Lỗi khi fetch hoặc render biểu đồ ${chartLabel} (${canvasId}):`, error);
        if (errorDiv) errorDiv.innerHTML += `<p>Lỗi khi tải biểu đồ ${chartLabel}: ${error.message}</p>`;
        ctx.clearRect(0, 0, canvasElement.width, canvasElement.height);
        ctx.font = "16px Arial";
        ctx.fillStyle = "red";
        ctx.fillText('Lỗi tải biểu đồ. Vui lòng thử lại.', 10, 50);
    }
}
