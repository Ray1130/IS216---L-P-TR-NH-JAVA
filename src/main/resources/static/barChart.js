// chartbar.js
// khai báo biến toàn cục để giữ instance của biểu đồ
// Biến toàn cục để giữ instance của biểu đồ, hữu ích nếu cần cập nhật mà không làm mới trang
let studentsByMajorChartInstance = null;

/**
 * Hàm để fetch dữ liệu từ API và vẽ/cập nhật biểu đồ số sinh viên theo ngành.
 * @param {string} canvasId - ID của phần tử canvas nơi biểu đồ sẽ được vẽ.
 * @param {string} apiUrl - URL của API backend để lấy dữ liệu.
 */
async function renderBarChart(canvasId, apiUrl) {
    const chartCanvas = document.getElementById(canvasId);// Lấy phần tử canvas theo ID
    const canvasElement = document.getElementById('barChart');

    if (!chartCanvas) { // Kiểm tra xem canvas có tồn tại không
        // Nếu không tìm thấy canvas, hiển thị thông báo lỗi
        console.error(`Không tìm thấy canvas với ID '${canvasId}'`);
        // Có thể hiển thị thông báo lỗi ở một vị trí chung trên dashboard
        const errorContainer = document.getElementById('chartErrorMessages'); // Giả sử có một div với ID này
        if (errorContainer) {
            errorContainer.innerHTML += `<p class="error">Lỗi: Không tìm thấy canvas cho biểu đồ ngành học.</p>`;
        }
        return;
    }
    const ctx = chartCanvas.getContext('2d');// nếu tìm thấy canvas, dòng này lấy ngữ cảnh 2D của nó để vẽ biểu đồ

    try {
        const response = await fetch(apiUrl);

        // Kiểm tra xem phản hồi có thành công không
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Lỗi HTTP: ${response.status} - ${errorText || response.statusText} (URL: ${apiUrl})`);
        }

        const chartAPIData = await response.json(); // API được kỳ vọng trả về { labels: [...], data: [...] }

        // Kiểm tra xem dữ liệu API có đúng định dạng không
        if (!chartAPIData || typeof chartAPIData.labels === 'undefined' || typeof chartAPIData.data === 'undefined') {
            console.error("Dữ liệu API không hợp lệ:", chartAPIData);
            throw new Error("Dữ liệu nhận được từ API không đúng định dạng mong đợi.");
        }

        // Hủy biểu đồ cũ nếu nó đã tồn tại
        if (studentsByMajorChartInstance) {
            studentsByMajorChartInstance.destroy();
        }

        // Tạo biểu đồ mới với dữ liệu từ API
        studentsByMajorChartInstance = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: chartAPIData.labels, // Nhãn (tên ngành) từ API
                datasets: [{
                    label: 'Số Lượng Sinh Viên',      // Nhãn của bộ dữ liệu
                    backgroundColor: 'rgba(55, 101, 209, 0.7)', // Màu nền cột
                    borderColor: 'rgb(55, 101, 209)',         // Màu viền cột
                    data: chartAPIData.data,                  // Dữ liệu (số lượng) từ API
                    borderWidth: 1                            // Độ rộng viền cột
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    x: {
                        title: {
                            display: true,
                            text: 'Ngành Học' // Tiêu đề trục X
                        }
                    },
                    y: {
                        beginAtZero: true, // Bắt đầu trục Y từ 0
                        title: {
                            display: true,
                            text: 'Số Lượng Sinh Viên' // Tiêu đề trục Y
                        },
                        ticks: {
                            precision: 0 // Đảm bảo số lượng sinh viên hiển thị là số nguyên
                        }
                    }
                },
                plugins: {
                    legend: {
                        display: true,
                        position: 'top'
                    },
                    title: {
                        display: true,
                        text: 'Biểu Đồ Số Lượng Sinh Viên Theo Ngành',
                        font: {
                            size: 16 // Điều chỉnh kích thước font tiêu đề
                        }
                    },
                    tooltip: {
                        callbacks: {
                            label: function (context) {
                                let label = context.dataset.label || '';
                                if (label) {
                                    label += ': ';
                                }
                                if (context.parsed.y !== null) {
                                    label += context.parsed.y + ' sinh viên';
                                }
                                return label;
                            }
                        }
                    }
                }
            }
        });
        console.log(`Biểu đồ '${canvasId}' đã được vẽ/cập nhật thành công.`);

    } catch (error) {
        console.error(`Lỗi khi tải hoặc vẽ biểu đồ '${canvasId}':`, error);
        const errorContainer = document.getElementById('chartErrorMessages'); // Giả sử có một div với ID này
        if (errorContainer) {
            errorContainer.innerHTML += `<p class="error">Lỗi tải dữ liệu cho biểu đồ ngành học: ${error.message}</p>`;
        } else {
            // Fallback nếu không có container lỗi cụ thể
            const canvasContainer = chartCanvas.parentElement;
            if (canvasContainer) {
                canvasContainer.innerHTML = `<p style="color: red; text-align: center; padding: 10px;">Lỗi: ${error.message}</p>`;
            }
        }
    }
}

