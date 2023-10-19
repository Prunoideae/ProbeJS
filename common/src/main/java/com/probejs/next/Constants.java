package com.probejs.next;

public class Constants {
    public static final String JAVA_PACKAGE_NAME = "@java";
    public static final String KUBEJS_PACKAGE_NAME = "@kubejs";


    public static String getJavaPackage(String packageName) {
        String[] parts = packageName.split("\\.");
        if (parts.length == 0) {
            return JAVA_PACKAGE_NAME;
        }
        return JAVA_PACKAGE_NAME + "/" + String.join("/", parts);
    }

    public static final String JSCONFIG = """
            {
                "compilerOptions": {
                    "target": "ES6",
                    "module": "ES6",
                    "esModuleInterop": true,
                    "forceConsistentCasingInFileNames": true,
                    "strict": true,
                    "skipLibCheck": true,
                    "typeRoots": [
                        "probe"
                    ],
                    "baseUrl": ".",
                    "paths": {
                        "@java/*": [
                            "probe/java/*"
                        ],
                        "@scripts/*": [
                            "probe/scripts/*"
                        ],
                        "@kubejs/eventgroups": [
                            "probe/events.d.ts"
                        ]
                    },
                    "lib": [
                        "ES2015",
                        "ES5"
                    ]
                }
            }
            """;
}
