package view;

import java.util.Scanner;
import controller.*;
import model.User;

public class MainView {
    private final LoginController loginController;
    private final DispenseController dispenseController;
    private final StockController stockController;
    private final ReportController reportController;

    public MainView(LoginController l, DispenseController d, StockController s, ReportController r) {
        this.loginController = l;
        this.dispenseController = d;
        this.stockController = s;
        this.reportController = r;
    }

    public void showMenu() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Nhập ID: ");
        String id = sc.nextLine();
        System.out.print("Nhập mật khẩu: ");
        String pw = sc.nextLine();

        User user = loginController.login(id, pw);

        switch (user.getRole()) {
            case "PATIENT":
                patientMenu(user);
                break;
            case "PHARMACIST":
                pharmacistMenu(user, sc);
                break;
            case "MANAGER":
                managerMenu(user, sc);
                break;
            default:
                System.out.println("Quyền truy cập không hợp lệ.");
        }
    }

    private void patientMenu(User user) {
        System.out.println("PATIENT MENU: chức năng xem đơn thuốc chưa được triển khai.");
        System.out.println("Vui lòng liên hệ nhà thuốc để biết chi tiết.");
    }

    private void pharmacistMenu(User user, Scanner sc) {
        System.out.println("PHARMACIST MENU: xuất thuốc, xem tồn kho");
        System.out.println("1. Xuất thuốc theo đơn");
        System.out.println("2. Xem cảnh báo tồn kho");
        System.out.print("Lựa chọn: ");
        String choice = sc.nextLine();

        switch (choice) {
            case "1":
                System.out.print("Nhập mã đơn thuốc: ");
                String prescriptionId = sc.nextLine();
                try {
                    dispenseController.processDispense(prescriptionId, user.getId());
                    System.out.println("Xuất thuốc thành công cho đơn " + prescriptionId);
                } catch (Exception e) {
                    System.out.println("Lỗi khi xuất thuốc: " + e.getMessage());
                }
                break;
            case "2":
                stockController.getLowStockAlert();
                break;
            default:
                System.out.println("Lựa chọn không hợp lệ.");
        }
    }

    private void managerMenu(User user, Scanner sc) {
        System.out.println("MANAGER MENU: quản lý kho, báo cáo");
        System.out.println("1. Xem báo cáo kho");
        System.out.println("2. Xem cảnh báo tồn kho");
        System.out.print("Lựa chọn: ");
        String choice = sc.nextLine();

        switch (choice) {
            case "1":
                reportController.printReport();
                break;
            case "2":
                stockController.getLowStockAlert();
                break;
            default:
                System.out.println("Lựa chọn không hợp lệ.");
        }
    }
}
