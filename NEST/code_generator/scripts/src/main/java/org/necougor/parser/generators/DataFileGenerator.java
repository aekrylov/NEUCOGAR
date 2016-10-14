package org.necougor.parser.generators;


import org.necougor.parser.model.python.BrainRegion;
import org.necougor.parser.model.python.Receptor;
import org.necougor.parser.util.CommonUtil;
import org.necougor.parser.util.FileReaderWriterUtil;
import org.necougor.parser.util.GeneratorUtil;
import org.necougor.parser.util.ParseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class DataFileGenerator {

    public static final String FILE_NAME = "data.py";

    public static final String KEY_NAME = "Name";
    public static final String KEY_NUMBER_NEURON = "NN";
    public static final String KEY_MODEL = "Model";
    public static final String KEY_IDS = "IDs";
    public static final String MODEL_TEMPLATE = "{'" + KEY_NAME + "': '%1$2s', '" + KEY_NUMBER_NEURON + "': %2$2s, '" + KEY_MODEL + "': '%3$2s', '" + KEY_IDS + "': nest.Create('%3$2s', %2$2s)}";

    public static final String VAR_TOTAL_NUMBER_OF_NEURON_NAME = "number_of_neuron";


    public static final String DEFAULT_VALUE_VAR = "DEFAULT";


    public static final String DEFAULT_VALUE_CONDITION = "if %1$2s < " + DEFAULT_VALUE_VAR + " : %1$2s = " + DEFAULT_VALUE_VAR;


    private static final Logger LOG = LoggerFactory.getLogger(DataFileGenerator.class);

    @Autowired
    private Environment env;


    @Autowired
    private Formatter formatter;

    public static Map<String, Integer> property;

    public void generate(Map<String, BrainRegion> pythonBrainRegionMap) {
        String data = generateData(pythonBrainRegionMap);
        String template = FileReaderWriterUtil.readTemplateFileToString(FILE_NAME);
        data = String.format(template, data);
        FileReaderWriterUtil.writeGeneratedStringToFile(data, FILE_NAME);
    }


    private String generateData(Map<String, BrainRegion> pythonBrainRegionMap) {
        List<String> allDataFileNames = CommonUtil.getAllDataFileNames(pythonBrainRegionMap);

        String count = env.getProperty("count");
        Integer cValue = count == null ? null : Integer.valueOf(count);


        property = new ReceptorPropertyCountGenerator(allDataFileNames, cValue).load();
        String data = "";


        //FIXME
        int total = 0;
        for (String key : property.keySet()) {
            total += property.get(key);
        }
        data = data + VAR_TOTAL_NUMBER_OF_NEURON_NAME + " = " + total + "\n";
        data = data + DEFAULT_VALUE_VAR + " = " + 10 + "\n";


        for (String key : pythonBrainRegionMap.keySet()) {
            final BrainRegion brainRegion = pythonBrainRegionMap.get(key);
            final List<Receptor> brainReceptor = brainRegion.getInnerReceptors();
            for (int i = 0; i < brainReceptor.size(); i++) {
                final String name1 = GeneratorUtil.createIndexVarName(brainRegion.getZoneName(), brainReceptor.get(i).getType());
                long recCount = property.get(name1);
                data = data + name1 + "_" + KEY_NUMBER_NEURON + " = int(" + recCount + " / " + total + " * " + VAR_TOTAL_NUMBER_OF_NEURON_NAME + ")\n";
                String cond = new Formatter().format(Locale.US, DEFAULT_VALUE_CONDITION, name1 + "_" + KEY_NUMBER_NEURON).toString();
                data = data + cond + "\n";
            }
            data += "\n";

            data = data + brainRegion.getZoneName() + " = (\n";


            for (int i = 0; i < brainReceptor.size(); i++) {
                Receptor receptor = brainReceptor.get(i);
                String stringModel = createStringModel(receptor, brainRegion);
                data = data + stringModel;
                if (i < brainReceptor.size() - 1 || brainReceptor.size() == 1) {
                    data += ",";
                }
                data += "\n";
            }
            data = data + ")\n";
            for (int i = 0; i < brainReceptor.size(); i++) {
                final String name1 = GeneratorUtil.createIndexVarName(brainRegion.getZoneName(), brainReceptor.get(i).getType());
                data = data + name1 + " = " + i + "\n";
            }
            data = data + "\n";
        }
        return data;
    }

    private String createStringModel(Receptor receptor, BrainRegion brainRegion) {
        formatter = new Formatter();
        String propertyName = GeneratorUtil.createIndexVarName(brainRegion.getZoneName(), receptor.getType());
        long count = property.get(propertyName);
        String name = GeneratorUtil.createVarName(brainRegion.getZoneName(), receptor.getType());
        String model = ParseUtil.getModelByReceptor(receptor).toString();

        String stringModel = formatter.format(MODEL_TEMPLATE, name, propertyName + "_" + KEY_NUMBER_NEURON, model).toString();
        return stringModel;
    }


}
