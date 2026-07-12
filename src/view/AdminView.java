package view;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

import controller.AdminController;
import model.Branch;
import model.Pharmacist;
import model.User;

public class AdminView {

    private static final NumberFormat MONEY = NumberFormat.getNumberInstance(Locale.US);

    static {
        MONEY.setMaximumFractionDigits(0);
        MONEY.setMinimumFractionDigits(0);
    }

    private final AdminController adminController;
    private final User user;

    public AdminView(AdminController adminController, User user) {
        this.adminController = adminController;
        this.user = user;
    }

    public void showMenu(Scanner sc) {
        while (true) {
            System.out.println("\n===== ADMIN MENU =====");
            System.out.println("1. Staff management");
            System.out.println("2. Branch management");
            System.out.println("3. Revenue report");
            System.out.println("0. Exit");
            System.out.print("Choice: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    staffMenu(sc);
                    break;
                case "2":
                    branchMenu(sc);
                    break;
                case "3":
                    revenueMenu(sc);
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    // ===== STAFF MENU =====
    private void staffMenu(Scanner sc) {
        while (true) {
            System.out.println("\n--- STAFF MANAGEMENT ---");
            System.out.println("1. View all staff");
            System.out.println("2. Add staff");
            System.out.println("3. Edit staff");
            System.out.println("4. Delete staff");
            System.out.println("0. Back");
            System.out.print("Choice: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    List<Pharmacist> all = adminController.getAllPharmacists();
                    if (all.isEmpty()) {
                        System.out.println("No staff found.");
                    } else {
                        System.out.printf("%-8s %-25s %-8s %-15s %-8s%n",
                                "ID", "Name", "Branch", "Phone", "Role");
                        System.out.println("-".repeat(70));
                        for (Pharmacist p : all) {
                            System.out.printf("%-8s %-25s %-8s %-15s %-8s%n",
                                    p.getPharmacistId(), p.getName(), p.getBranchId(),
                                    p.getUsername(), p.getRole().name());
                        }
                    }
                    pause(sc);
                    break;

                case "2":
                    System.out.print("Full name: ");
                    String name = sc.nextLine();
                    System.out.print("Branch ID (e.g., B001): ");
                    String branchId = sc.nextLine();
                    System.out.print("Phone number: ");
                    String phone = sc.nextLine();
                    System.out.print("Password: ");
                    String password = sc.nextLine();
                    System.out.print("Role (STAFF/ADMIN): ");
                    String role = sc.nextLine();
                    try {
                        Pharmacist added = adminController.addPharmacist(name, branchId, phone, password, role);
                        System.out.println("Added successfully: " + added.getPharmacistId());
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    pause(sc);
                    break;

                case "3":
                    System.out.print("Enter pharmacist ID to edit (e.g., P001): ");
                    String editId = sc.nextLine();
                    Pharmacist cur = adminController.getPharmacistById(editId);
                    if (cur == null) {
                        System.out.println("Not found: " + editId);
                        pause(sc);
                        break;
                    }
                    System.out.println("Current: " + cur.getName() + " | " + cur.getBranchId()
                            + " | " + cur.getUsername() + " | " + cur.getRole().name());
                    System.out.print("New name (Enter to keep): ");
                    String newName = sc.nextLine();
                    System.out.print("New branch ID (Enter to keep): ");
                    String newBranch = sc.nextLine();
                    System.out.print("New phone (Enter to keep): ");
                    String newPhone = sc.nextLine();
                    System.out.print("New role STAFF/ADMIN (Enter to keep): ");
                    String newRole = sc.nextLine();
                    try {
                        Pharmacist edited = adminController.editPharmacist(editId, newName, newBranch, newPhone, newRole);
                        System.out.println("Updated: " + edited.getPharmacistId());
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    pause(sc);
                    break;

                case "4":
                    System.out.print("Enter pharmacist ID to delete (e.g., P001): ");
                    String delId = sc.nextLine();
                    System.out.print("Confirm delete " + delId + "? (yes/no): ");
                    if ("yes".equalsIgnoreCase(sc.nextLine().trim())) {
                        try {
                            adminController.deletePharmacist(delId);
                            System.out.println("Deleted: " + delId);
                        } catch (Exception e) {
                            System.out.println("Error: " + e.getMessage());
                        }
                    } else {
                        System.out.println("Cancelled.");
                    }
                    pause(sc);
                    break;

                case "0":
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    // ===== BRANCH MENU =====
    private void branchMenu(Scanner sc) {
        while (true) {
            System.out.println("\n--- BRANCH MANAGEMENT ---");
            System.out.println("1. View all branches");
            System.out.println("2. Add branch");
            System.out.println("3. Edit branch");
            System.out.println("4. Delete branch");
            System.out.println("0. Back");
            System.out.print("Choice: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    List<Branch> branches = adminController.getAllBranches();
                    if (branches.isEmpty()) {
                        System.out.println("No branches found.");
                    } else {
                        System.out.printf("%-8s %-20s %-25s %-15s%n", "ID", "Name", "Address", "Phone");
                        System.out.println("-".repeat(70));
                        for (Branch b : branches) {
                            System.out.printf("%-8s %-20s %-25s %-15s%n",
                                    b.getBranchId(), b.getBranchName(), b.getAddress(), b.getPhone());
                        }
                    }
                    pause(sc);
                    break;

                case "2":
                    System.out.print("Branch name: ");
                    String bname = sc.nextLine();
                    System.out.print("Address: ");
                    String baddress = sc.nextLine();
                    System.out.print("Phone: ");
                    String bphone = sc.nextLine();
                    try {
                        Branch added = adminController.addBranch(bname, baddress, bphone);
                        System.out.println("Branch added: " + added.getBranchId() + " - " + added.getBranchName());
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    pause(sc);
                    break;

                case "3":
                    System.out.print("Enter branch ID to edit (e.g., B001): ");
                    String editBid = sc.nextLine();
                    Branch cb = adminController.getBranchById(editBid);
                    if (cb == null) {
                        System.out.println("Not found: " + editBid);
                        pause(sc);
                        break;
                    }
                    System.out.println("Current: " + cb.getBranchName() + " | " + cb.getAddress() + " | " + cb.getPhone());
                    System.out.print("New name (Enter to keep): ");
                    String newBname = sc.nextLine();
                    System.out.print("New address (Enter to keep): ");
                    String newBaddr = sc.nextLine();
                    System.out.print("New phone (Enter to keep): ");
                    String newBphone = sc.nextLine();
                    try {
                        Branch edited = adminController.editBranch(editBid, newBname, newBaddr, newBphone);
                        System.out.println("Updated: " + edited.getBranchId());
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    pause(sc);
                    break;

                case "4":
                    System.out.print("Enter branch ID to delete (e.g., B001): ");
                    String delBid = sc.nextLine();
                    System.out.print("Confirm delete branch " + delBid + "? (yes/no): ");
                    if ("yes".equalsIgnoreCase(sc.nextLine().trim())) {
                        try {
                            adminController.deleteBranch(delBid);
                            System.out.println("Deleted branch: " + delBid);
                        } catch (Exception e) {
                            System.out.println("Error: " + e.getMessage());
                        }
                    } else {
                        System.out.println("Cancelled.");
                    }
                    pause(sc);
                    break;

                case "0":
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    // ===== REVENUE MENU =====
    private void revenueMenu(Scanner sc) {
        while (true) {
            System.out.println("\n--- REVENUE REPORT ---");
            System.out.println("1. View revenue of a specific branch");
            System.out.println("2. View revenue of all branches");
            System.out.println("0. Back");
            System.out.print("Choice: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    System.out.print("Enter branch ID (e.g., B001): ");
                    String bid = sc.nextLine().trim();
                    System.out.println("1. Last 30 days");
                    System.out.println("2. All time");
                    System.out.print("Choice: ");
                    String periodChoice = sc.nextLine().trim();
                    boolean last30 = "1".equals(periodChoice);
                    try {
                        double revenue = adminController.getBranchRevenue(bid, last30);
                        Branch branch = adminController.getBranchById(bid);
                        String bname = branch != null ? branch.getBranchName() : bid;
                        System.out.println("===== REVENUE: " + bname + " (" + (last30 ? "Last 30 days" : "All time") + ") =====");
                        System.out.println("Total Revenue: " + MONEY.format(revenue) + " VND");
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    pause(sc);
                    break;

                case "2":
                    System.out.println("1. Last 30 days");
                    System.out.println("2. All time");
                    System.out.print("Choice: ");
                    String allPeriodChoice = sc.nextLine().trim();
                    boolean allLast30 = "1".equals(allPeriodChoice);
                    try {
                        Map<String, Double> revenues = adminController.getAllBranchRevenues(allLast30);
                        List<Branch> allBranches = adminController.getAllBranches();
                        Map<String, String> branchNames = allBranches.stream()
                                .collect(java.util.stream.Collectors.toMap(Branch::getBranchId, Branch::getBranchName, (a, b) -> a));

                        System.out.println("===== ALL BRANCH REVENUES (" + (allLast30 ? "Last 30 days" : "All time") + ") =====");
                        System.out.printf("%-8s %-20s %20s%n", "ID", "Branch Name", "Revenue (VND)");
                        System.out.println("-".repeat(52));
                        double grandTotal = 0;
                        for (Map.Entry<String, Double> entry : revenues.entrySet()) {
                            String branchId = entry.getKey();
                            double rev = entry.getValue();
                            String bname = branchNames.getOrDefault(branchId, branchId);
                            System.out.printf("%-8s %-20s %20s%n", branchId, bname, MONEY.format(rev));
                            grandTotal += rev;
                        }
                        System.out.println("-".repeat(52));
                        System.out.printf("%-28s %20s%n", "TOTAL", MONEY.format(grandTotal));
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    pause(sc);
                    break;

                case "0":
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private void pause(Scanner sc) {
        System.out.print("\nPress Enter to continue...");
        sc.nextLine();
    }
}
