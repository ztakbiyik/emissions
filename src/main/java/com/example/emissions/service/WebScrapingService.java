package com.example.emissions.service;

import com.example.emissions.model.Emission;
import com.example.emissions.model.ParsedDescription;
import com.example.emissions.model.ParsedEmission;
import com.example.emissions.model.ParsedEmissionPrediction;
import com.example.emissions.repository.EmissionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WebScrapingService {

    private final EmissionRepository emissionRepository;


    public String downloadXml(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "text/xml");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder xmlStringBuilder = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                xmlStringBuilder.append(line);
            }
            in.close();

            return xmlStringBuilder.toString();
        } else {
            throw new IOException("Failed to download XML: " + responseCode);
        }

    }


    public List<ParsedEmission> parseEmissionData() {

        List<ParsedEmission> parsedEmissions = new ArrayList<>();
        try {

            String xmlData = downloadXml("https://cdr.eionet.europa.eu/ie/eu/mmr/art04-13-14_lcds_pams_projections/projections/envvxoklg/MMR_IRArticle23T1_IE_2016v2.xml");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            StringReader sr = new StringReader(xmlData);
            InputSource is = new InputSource(sr);
            Document document = builder.parse(is);

            NodeList rowList = document.getElementsByTagName("Row");


            for (int i = 0; i < rowList.getLength(); i++) {
                Node rowNode = rowList.item(i);

                if (rowNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element rowElement = (Element) rowNode;

                    String year = parseElementValue(rowElement, "Year");
                    String scenario = parseElementValue(rowElement, "Scenario");
                    String value = parseElementValue(rowElement, "Value");

                    if (checkIfEmissionIsValid(year, scenario, value)) {
                        value = emissionValueToDecimal(value);
                        String category = parseElementValue(rowElement, "Category__1_3");
                        String gasUnit = parseElementValue(rowElement, "Gas___Units");
                        String NK = parseElementValue(rowElement, "NK");

                        ParsedEmission parsedEmission = new ParsedEmission(category, year, scenario, gasUnit, NK, value);
                        parsedEmissions.add(parsedEmission);
                    }
                }
            }

        } catch (Exception e) {

            System.out.println(e.getLocalizedMessage());
        }


        return parsedEmissions;
    }

    public static String emissionValueToDecimal(String value) {
        try {
            double numericValue = Double.parseDouble(value);
            DecimalFormat decimalFormat = new DecimalFormat("#.##############");
            return decimalFormat.format(numericValue);
        } catch (NumberFormatException e) {
            return "";
        }
    }


    private static boolean checkIfEmissionIsValid(String year, String scenario, String value) {
        return "2023".equals(year) && "WEM".equals(scenario) && checkValue(value);
    }

    private static boolean checkValue(String value) {
        try {
            double val = Double.parseDouble(value);
            return val >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static String parseElementValue(Element element, String tagName) {
        NodeList nodeList = element.getElementsByTagName(tagName);
        Node node = nodeList.item(0);
        if (node != null && node.hasChildNodes()) {
            Node childNode = node.getFirstChild();
            return childNode.getNodeValue();
        }
        return "";
    }

    //parse json
    public static List<ParsedEmissionPrediction> parseEmissionPredictions() {
        List<ParsedEmissionPrediction> emissions = new ArrayList<>();

        try{
            Path path = ResourceUtils.getFile("src/main/resources/data/EmissionData.json").toPath();

            File file = new File(path.toUri());

            InputStream inputStream = new FileInputStream(file);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(inputStream);
            JsonNode emissionsNode = rootNode.get("Emissions");

            for (JsonNode emissionNode : emissionsNode) {
                String category = emissionNode.get("Category").asText();
                String gasUnits = emissionNode.get("Gas Units").asText();
                double value = emissionNode.get("Value").asDouble();
                String formattedValue = emissionValueToDecimal(String.valueOf(value));

                ParsedEmissionPrediction e = new ParsedEmissionPrediction(category, gasUnits, formattedValue);
                emissions.add(e);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return emissions;
    }


    //parse description
    public static List<ParsedDescription> parseDescription() {
        List<ParsedDescription> descriptions = new ArrayList<>();

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.ipcc-nggip.iges.or.jp/EFDB/find_ef.php"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String content = response.body();

            String[] lines = content.split("ipccTree.add");

            for (String line : lines) {
                if (line.contains("'")) {
                    String[] parts = line.split(",");
                    if (parts.length >= 4) {
                        String description = parts[2].trim().replaceAll("[^0-9A-Za-z. ]", "");
                        ParsedDescription category = new ParsedDescription(description);
                        descriptions.add(category);
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return descriptions;
    }

    @PostConstruct
    private void runParser() {
        List<ParsedEmission> parsedEmissions = parseEmissionData();
        List<ParsedDescription> parsedDescriptions = parseDescription();
        List<ParsedEmissionPrediction> parsedEmissionPredictions = parseEmissionPredictions();

        createAndSaveEmissionData(parsedEmissions, parsedDescriptions, parsedEmissionPredictions);

    }

    @Transactional
    private void createAndSaveEmissionData(List<ParsedEmission> parsedEmissions,
                                           List<ParsedDescription> parsedDescriptions,
                                           List<ParsedEmissionPrediction> parsedEmissionPredictions) {

        parsedEmissions.forEach(parsedEmission -> {
            Emission combinedEmission = new Emission();
            parsedEmissionPredictions.stream()
                    .filter(pe -> parsedEmission.getCategory().equals(pe.getCategory()) &&
                            parsedEmission.getGasUnit().equals(pe.getGasUnits()))
                    .findFirst()
                    .ifPresentOrElse(emissionPrediction -> {
                        parsedEmissionToEmission(parsedDescriptions, parsedEmission, combinedEmission);
                        combinedEmission.setPredictedValue(emissionPrediction.getValue());
                    }, () -> {
                        parsedEmissionToEmission(parsedDescriptions, parsedEmission, combinedEmission);
                    });
            emissionRepository.save(combinedEmission);
        });


//        parsedEmissionPredictions.forEach(emissionPrediction -> {
//            if (parsedEmissions.stream()
//                    .noneMatch(parsedEmission -> emissionPrediction.getCategory().equals(parsedEmission.getCategory()) &&
//                            emissionPrediction.getGasUnits().equals(parsedEmission.getGasUnit()))) {
//                Emission combinedEmission = new Emission();
//                combinedEmission.setCategory(emissionPrediction.getCategory());
//                combinedEmission.setGasUnits(emissionPrediction.getGasUnits());
//                combinedEmission.setPredictedValue(emissionPrediction.getValue());
//                parsedDescriptions.stream()
//                        .filter(parsedDescription -> parsedDescription.getDescription().contains(emissionPrediction.getCategory()))
//                        .findFirst()
//                        .ifPresent(parsedDescription -> combinedEmission.setDescription(parsedDescription.getDescription()));
//                emissionRepository.save(combinedEmission);
//            }
//
//        });
    }

    private static void parsedEmissionToEmission(List<ParsedDescription> parsedDescriptions, ParsedEmission parsedEmission, Emission combinedEmission) {
        combinedEmission.setCategory(parsedEmission.getCategory());
        combinedEmission.setGasUnits(parsedEmission.getGasUnit());
        combinedEmission.setActualValue(parsedEmission.getValue());
        parsedDescriptions.stream()
                .filter(parsedDescription -> parsedDescription.getDescription().contains(parsedEmission.getCategory()))
                .findFirst()
                .ifPresent(parsedDescription -> combinedEmission.setDescription(parsedDescription.getDescription()));
    }

}