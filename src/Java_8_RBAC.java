import java.io.*;
import java.util.*;
import java.util.Arrays;

import com.sun.tools.javac.util.ArrayUtils;


class Node {
    // List of ascendants.
    ArrayList<Node> ascendant = new ArrayList<>();
    Node descendant;

    String roleName;
    String descendantName = null;
}

class NRBAC {

    //create a HashMap to store role hierarchy.
    static HashMap<String, Node> roleHierarchy = new HashMap<>();
    static HashMap<String, Node> addedRoles = new HashMap<>();
    static Map<String, List<String>> permissionHash = new HashMap<>();
    static ArrayList<String> descendantRole = new ArrayList<>();
    static String[] allRoles;
    static ArrayList<String> runningRoles = new ArrayList<>();
    static ArrayList<String> allObjects = new ArrayList<>();
    static ArrayList<String> listRoles = new ArrayList<>();
    static ArrayList<String> grantRoles = new ArrayList<>();
    static ArrayList<String> grantAccessRights = new ArrayList<>();
    static ArrayList<String> grantObjects = new ArrayList<>();
    static String twoD[][];

    public static void main(String[] args) throws Exception {

        //read roles from roleHierarchy.txt
        File readRoleHierarchy = new File("roleHierarchy.txt");
        BufferedReader bufferedReader = new BufferedReader(new FileReader(readRoleHierarchy));
        // String to store roles from txt file.
        String rolesString;
        // String array to store the .split roles from String str to roles[0] and roles[1].
        //String[] allRoles;

        int readLines = 1;
        boolean exitLoop = false;
        first:
        {
            while (!exitLoop) {
                second:
                {
                    while ((rolesString = bufferedReader.readLine()) != null) {

                        allRoles = rolesString.split("\\s+");
                        //create a new role from the Role class below
                        Node newRole = new Node();
                        // roleName stored.
                        newRole.roleName = allRoles[0];
                        if (addedRoles.containsKey(newRole.roleName) && (!addedRoles.get(allRoles[0]).descendantName.equals(allRoles[1]))) {

                            System.out.print("Invalid line is found in roleHierarchy.txt: line <" + readLines + ", " + allRoles[0] +
                                    " " + allRoles[1] + ">. ");
                            promptEnterKey();
                            break second;
                        }

                        readLines++;
                        // descendantName stored.
                        newRole.descendantName = allRoles[1];
                        // Put elements in the map. (R2's name, R2's role class)
                        addedRoles.put(newRole.roleName, newRole);
                        runningRoles.add(allRoles[0]);
                    }
                    exitLoop = true;
                }
            }
        }

        bufferedReader.close();
        roleHierarchy.putAll(addedRoles);

        // Get a set of the entries.
        Set<HashMap.Entry<String, Node>> setMap = addedRoles.entrySet();

        // Display the set.
        for(HashMap.Entry<String, Node> printSet : setMap) {

            // create a String of setDescendants and get values from the set of entries.
            String setDescendants = printSet.getValue().descendantName;
            // Search roleHierarchy for descendants Key value, create if it doesn't exist and put roleHierarchy and
            // descendantsRole String.
            if (!roleHierarchy.containsKey(setDescendants)) {

                Node newRole = new Node();
                newRole.roleName = setDescendants;
                roleHierarchy.put(setDescendants, newRole);
                descendantRole.add(setDescendants);
            }

            roleHierarchy.get(setDescendants).ascendant.add(printSet.getValue());
            printSet.getValue().descendant = roleHierarchy.get(setDescendants);
            //System.out.println("Role: " + printSet.getKey() + ",\tDescendant: " + printSet.getValue().descendantName);
        }

        for (int i = 0; i < descendantRole.size(); i++) {

            System.out.println();
            printTree(descendantRole.get(i), 0);
        }
        System.out.println();

        //read roles from roleHierarchy.txt
        File readResourceObjects = new File("resourceObjects.txt");
        BufferedReader brResourceObjects = new BufferedReader(new FileReader(readResourceObjects));
        // String to store roles from txt file.
        String objectsString;
        String[] objectsArray;

        System.out.print("\t");
        for(String element3: descendantRole) {

            allObjects.add(element3);
            listRoles.add(element3);
        }
        for(String element2: runningRoles) {

            allObjects.add(element2);
            listRoles.add(element2);
        }
        while((objectsString = brResourceObjects.readLine()) != null) {

            objectsArray = objectsString.split("\\s+");
            for(String element: objectsArray) {

                allObjects.add(element);
            }
        }
        for(String element4: allObjects) {

            System.out.print(element4 + "\t\t");
        }
        System.out.println();

        int rows = listRoles.size();
        int columns = allObjects.size();
        twoD = new String[rows][columns];

        // GRID PRINTING
        int x, y, z = 0;
//        for(x = 0; x < rows; x++) {

//            for(y = 0; y < columns; y++) {
//                twoD[x][y] = "----";
//                z++;
//            }
//        }

        for(x = 0; x < rows; x++) {

            System.out.print(listRoles.get(x));
            System.out.print("\t");
            for (y = 0; y < columns; y++) {

                System.out.print(twoD[x][y] + "\t");
            }
            System.out.println();
        }

        System.out.println("\nReadPermissionToRoles block: ");

        File readPermissionToRoles = new File("permissionsToRoles.txt");
        BufferedReader brPermissionToRoles = new BufferedReader(new FileReader(readPermissionToRoles));
        String permissionRolesString;
        String[] permissionRolesArray;

        while((permissionRolesString = brPermissionToRoles.readLine()) != null) {

            permissionRolesArray = permissionRolesString.split("\\s+");
            grantRoles.add(permissionRolesArray[0]);
            grantAccessRights.add(permissionRolesArray[1]);
            grantObjects.add(permissionRolesArray[2]);
        }

        System.out.println("\ngranting roles: " + grantRoles);
        System.out.println("access rights: " + grantAccessRights);
        System.out.println("granted objects: " + grantObjects);
        System.out.println();

        for(int k = 0; k < grantRoles.size(); k++) {

            assignToGrid(grantRoles.get(k), grantAccessRights.get(k), grantObjects.get(k));

            permissionHash.computeIfAbsent(grantObjects.get(k), rightsList -> new ArrayList<>()).add(grantAccessRights.get(k));



            if (roleHierarchy.get(grantObjects.get(k)).descendantName != null) {

                if(!permissionHash.get(roleHierarchy.get(grantObjects.get(k)).descendantName).contains(grantAccessRights.get(k))) {
                    //System.out.println("Rights1");
                    permissionHash.computeIfAbsent(roleHierarchy.get(grantObjects.get(k)).descendantName, rightsList -> new ArrayList<>()).add(grantAccessRights.get(k));
                }
                if(!permissionHash.get(roleHierarchy.get(grantObjects.get(k)).descendantName).contains("own")) {
                    //System.out.println("Own1");
                    permissionHash.computeIfAbsent(roleHierarchy.get(grantObjects.get(k)).descendantName, rightsList -> new ArrayList<>()).add("own");
                }
                if(!permissionHash.get(grantObjects.get(k)).contains("control")) {
                    //System.out.println("Control1");
                    permissionHash.computeIfAbsent(grantObjects.get(k), rightsList -> new ArrayList<>()).add("control");
                }
                //permissionHash.computeIfAbsent(grantObjects.get(k), rightsList -> new ArrayList<>()).add("control");
                if(!permissionHash.get(roleHierarchy.get(grantObjects.get(k)).descendantName).contains("control")) {
                    //System.out.println("Control2");
                    permissionHash.computeIfAbsent(roleHierarchy.get(grantObjects.get(k)).descendantName, rightsList -> new ArrayList<>()).add("control");
                }
//
                assignToGrid(grantObjects.get(k), grantAccessRights.get(k), roleHierarchy.get(grantObjects.get(k)).descendantName);
            }

            System.out.println(permissionHash);
            System.out.println();
            //System.out.println("R1" + " = " + permissionHash.get("R1"));
        }

        // Print 2nd Grid
        System.out.println();
        System.out.print("\t");
        for(String element4: allObjects) {
            //allObjects.add(element4);
            System.out.print(element4 + "\t\t");
        }
        System.out.println();
        for(x = 0; x < rows; x++) {

            System.out.print(listRoles.get(x));
            System.out.print("\t");
            for (y = 0; y < columns; y++) {

                System.out.print(twoD[x][y] + "\t");
            }
            System.out.println();
        }
//        Print 2D Array
//        System.out.println();
//        for(int i = 0; i < twoD.length; i++) {
//            System.out.println(Arrays.toString(twoD[i]));
//        }
    }


    public static void assignToGrid(String roles, String accessRights, String objects) {

        twoD[listRoles.indexOf(roles)][allObjects.indexOf(objects)] = accessRights;
    }

    public static void promptEnterKey(){
        System.out.println("Press \"ENTER\" to continue...");
        try {
            int keyboardRead = System.in.read(new byte[2]);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void printTree(String roleName, int row) throws NullPointerException {
        row++;
        System.out.println(roleName);
        String treeValues;
        ArrayList<Node> ascendants = roleHierarchy.get(roleName).ascendant;
        if (!ascendants.isEmpty()) {
            for (int i = 0; i < ascendants.size(); i++) {
                if ((i + 1) == ascendants.size()) {
                    treeValues = "└──";
                } else {
                    treeValues = "├──";
                }
                if (row > 1) {
                    String indent = String.format("%" + ((row - 1) * 3) + "s", " ");
                    System.out.print(indent + treeValues);
                } else {
                    System.out.print(treeValues);
                }
                String ascendant = roleHierarchy.get(ascendants.get(i).roleName).roleName;
                printTree(ascendant, row);
            }
        }
    }
}