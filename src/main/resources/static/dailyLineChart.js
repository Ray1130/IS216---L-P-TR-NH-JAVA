/**
 * Hàm fetch dữ liệu và vẽ biểu đồ đường cho lượt đăng ký theo ngày.
 * Dùng được chung cho các biểu đồ tương tự (giờ, tuần, tháng).
 */
async function renderLineChart({ canvasId, apiUrl, chartLabel, xAxisLabel, yAxisLabel, reverseData = false }) { //khai báo 1 hàm bất đồng bộ (async) để lấy dữ liệu và vẽ biểu đồ
    const canvasElement = document.getElementById(canvasId); //lấy canvas bằng cách sử dụng ID của nó
    const errorDiv = document.getElementById('chartErrorMessages');// lấy phần tử hiển thị lỗi nếu có, để hiển thị thông báo lỗi nếu cần

    if (!canvasElement) {
        console.error(`Không tìm thấy canvas với ID "${canvasId}"`);
        if (errorDiv) errorDiv.innerHTML += `<p>Lỗi: Không tìm thấy canvas '${canvasId}'</p>`;
        return;
    }

    const ctx = canvasElement.getContext('2d');// lấy context vẽ 2d của canvascanvas

    //xử lý lỗi 
    try {
        const response = await fetch(apiUrl);

        if (!response.ok) {// kiểm tra xem phản hồi có thành công không
            // Nếu không thành công, hiển thị thông báo lỗi
            let errorMessage = `Lỗi API: ${response.status} ${response.statusText}`;
            try {
                const errorData = await response.json();
                errorMessage += ` - ${errorData.message || errorData.body || JSON.stringify(errorData)}`;
            } catch (_) { }
            console.error(errorMessage);
            ctx.clearRect(0, 0, canvasElement.width, canvasElement.height);
            ctx.font = "16px Arial"; ctx.fillStyle = "red";
            ctx.fillText(`Không thể tải dữ liệu cho biểu đồ.`, 10, 50);
            if (errorDiv) errorDiv.innerHTML += `<p>${errorMessage}</p>`;
            return;
        }

        const chartServerData = await response.json();
        if (reverseData) {
            chartServerData.labels.reverse();// Đảo ngược thứ tự ngày
            chartServerData.data.reverse();// Đảo ngược số lượt đăng ký
        }

        // ➕ Thêm dòng xử lý định dạng nhãn trục X
        chartServerData.labels = chartServerData.labels.map(label => {// tạo 1 mảng labels mới từ mảng labels của dữ liệu trả về từ API
            const [date] = label.split(' ');
            const [year, month, day] = date.split('-');
            return `${day}/${month}/${year}`;
        });

        if (!chartServerData.labels || !chartServerData.data) {
            console.error("Dữ liệu từ API không hợp lệ:", chartServerData);
            ctx.clearRect(0, 0, canvasElement.width, canvasElement.height);
            ctx.font = "16px Arial"; ctx.fillStyle = "orange";
            ctx.fillText("Dữ liệu không hợp lệ.", 10, 50);
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
                    backgroundColor: 'rgba(84, 181, 209, 0.6)',
                    borderColor: 'rgb(84, 181, 209)',
                    pointBorderColor: 'rgb(84, 181, 209)',
                    pointBackgroundColor: 'white',
                    pointHoverBackgroundColor: 'rgb(84, 181, 209)',
                    pointHoverBorderColor: 'white',
                    borderWidth: 3,
                    pointRadius: 4,
                    fill: true
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        beginAtZero: true,// Bắt đầu trục Y từ 0
                        title: {
                            display: true,
                            text: yAxisLabel
                        }
                    },
                    x: {
                        grid: {
                            display: true,
                            offset: false
                        },
                        ticks: {
                            autoSkip: true,
                            maxRotation: 45,
                            minRotation: 0,
                            font: { size: 10 },
                            callback: function (value, index, ticks) {
                                // Giả sử bạn dùng kiểu thời gian dạng ISO 8601 như "2024-10-10 00:00:00"
                                const label = this.getLabelForValue(value);
                                return label.split(' ')[0]; // Lấy phần trước dấu cách (yyyy-mm-dd)
                            },
                            title: {
                                display: true,
                                text: xAxisLabel
                            }
                        }
                    }
                },
                plugins: {
                    legend: {
                        display: true,// Hiển thị chú giải
                        position: 'top',//đặt chú giải ở trên cùng
                        labels: { font: { size: 12 } }
                    },
                    tooltip: {
                        mode: 'index',
                        intersect: false,
                        callbacks: {
                            label: function (context) {
                                let label = context.dataset.label || '';
                                return label ? `${label}: ${context.parsed.y} lượt` : `${context.parsed.y} lượt`;
                            }
                        }
                    }
                }
            }
        });

    } catch (error) {
        const errMsg = `Lỗi khi fetch/render biểu đồ ${chartLabel} (${canvasId}): ${error.message}`;
        console.error(errMsg, error);
        ctx.clearRect(0, 0, canvasElement.width, canvasElement.height);
        ctx.font = "16px Arial"; ctx.fillStyle = "red";
        ctx.fillText('Không thể hiển thị biểu đồ.', 10, 50);
        if (errorDiv) errorDiv.innerHTML += `<p>${errMsg}</p>`;
    }
}

