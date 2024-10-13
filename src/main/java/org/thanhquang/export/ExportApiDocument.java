package org.thanhquang.export;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.reflections.Reflections;
import org.springframework.web.bind.annotation.*;
import org.thanhquang.dto.ResDto;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;


/**
 * This class is responsible for exporting API documentation by scanning controller classes
 * annotated with @RestController and their associated request mappings.
 */

public class ExportApiDocument {

    private ExportApiDocument() {
    }

    public static void run(String packagePath) {
        List<ResDto> data = scanPackageAndGetApis(packagePath);
        exportToJsonFile(data);
    }

    /**
     * Scans the specified package for controller classes and extracts their request mappings,
     * returning a list of API endpoint details.
     *
     * @param packagePath the package to scan for controller classes (e.g., "com.example.quang.controller").
     * @return a list of ResDto objects containing details of controller methods and their mappings.
     */
    private static List<ResDto> scanPackageAndGetApis(String packagePath) {
        List<ResDto> result = new ArrayList<>();
        Reflections reflections = new Reflections(packagePath);

        // Get all controller classes with @RestController annotation
        Set<Class<?>> controllers = reflections.getTypesAnnotatedWith(RestController.class);

        for (Class<?> controller : controllers) {
            String[] controllerPaths = getControllerPaths(controller);

            for (Method method : controller.getDeclaredMethods()) {
                Optional<Annotation> mappingAnnotation = getMappingAnnotation(method);
                if (mappingAnnotation.isPresent()) {
                    ResDto resDto = createResDto(controller, method, mappingAnnotation.get(), controllerPaths);
                    result.add(resDto);
                }
            }
        }
        return result;
    }

    private static void exportToJsonFile(List<ResDto> resDtos) {
        if (!resDtos.isEmpty()) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try (FileWriter writer = new FileWriter("apiExport.json")) {
                gson.toJson(resDtos, writer);
                System.out.println("Writing JSON file successful.");
            } catch (IOException e) {
                System.out.println("Error writing JSON file: " + e.getMessage());
            }
        } else {
            System.out.println("NOT FOUND Apis!!!");
        }
    }

    private static String[] getControllerPaths(Class<?> controller) {
        return Arrays.stream(controller.getDeclaredAnnotations())
                .filter(RequestMapping.class::isInstance)
                .map(annotation -> ((RequestMapping) annotation).value())
                .findFirst()
                .orElse(new String[0]);
    }

    private static Optional<Annotation> getMappingAnnotation(Method method) {
        return Arrays.stream(method.getAnnotations())
                .filter(annotation -> annotation instanceof RequestMapping ||
                        annotation instanceof GetMapping ||
                        annotation instanceof PostMapping ||
                        annotation instanceof PutMapping ||
                        annotation instanceof DeleteMapping)
                .findFirst();
    }

    private static ResDto createResDto(Class<?> controller, Method method, Annotation annotation, String[] controllerPaths) {
        ResDto resDto = new ResDto();
        resDto.setControllerName(controller.getSimpleName());
        resDto.setControllerPath(controller.getName());
        resDto.setMethodName(method.getName());
        resDto.setMethodType(convertMethodType(annotation.annotationType().getSimpleName()));

        String[] methodPath = getMethodPaths(annotation);
        List<String> methodUrls = buildMethodUrls(controllerPaths, methodPath);
        resDto.setMethodUrl(methodUrls);

        return resDto;
    }

    private static List<String> buildMethodUrls(String[] controllerPaths, String[] methodPath) {
        if (controllerPaths.length == 0) {
            return formatPaths(methodPath);
        }
        return Arrays.stream(controllerPaths)
                .flatMap(controllerPath -> formatPaths(controllerPath, methodPath).stream())
                .collect(Collectors.toList());
    }

    private static List<String> formatPaths(String[] paths) {
        return Arrays.stream(paths)
                .map(path -> path.startsWith("/") ? path : "/" + path)
                .collect(Collectors.toList());
    }

    private static List<String> formatPaths(String controllerPath, String[] methodPath) {
        if (methodPath.length == 0) {
            return Collections.singletonList(controllerPath.startsWith("/") ? controllerPath : "/" + controllerPath);
        }
        return Arrays.stream(methodPath)
                .map(path -> controllerPath.startsWith("/") ? controllerPath + path : "/" + controllerPath + path)
                .collect(Collectors.toList());
    }

    private static String[] getMethodPaths(Annotation annotation) {
        if (annotation instanceof RequestMapping) {
            return ((RequestMapping) annotation).value();
        } else if (annotation instanceof GetMapping) {
            return ((GetMapping) annotation).value();
        } else if (annotation instanceof PostMapping) {
            return ((PostMapping) annotation).value();
        } else if (annotation instanceof PutMapping) {
            return ((PutMapping) annotation).value();
        } else if (annotation instanceof DeleteMapping) {
            return ((DeleteMapping) annotation).value();
        }
        return new String[0];
    }

    private static String convertMethodType(String methodType) {
        switch (methodType) {
            case "GetMapping":
                return "GET";
            case "PostMapping":
                return "POST";
            case "PutMapping":
                return "PUT";
            case "DeleteMapping":
                return "DELETE";
            default:
                throw new IllegalStateException("Unexpected value: " + methodType);
        }
    }
}
