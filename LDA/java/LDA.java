import javax.swing.text.html.parser.Entity;
import java.util.*;

public class LDA {
    public Map<String,List<String>> run(List<String> targets, int depth, int epochs, double alpha, double beta) {
        List<Map<String,List<String>>> topicDoc = new LinkedList<>();
        Map<String,List<String>> docTopic = new HashMap<>();
        int[] kArray = {3,7,13,19};
        depth = Math.min(depth,4);
        List<String> baseTopic = new ArrayList<>(targets);
        topicDoc.add(new HashMap<>());
        topicDoc.get(0).put("root",baseTopic);
        for (int i=0; i<targets.size(); i++) {
            docTopic.put(targets.get(i), new ArrayList<>());
        }
        for (int i=0; i<depth; i++) {
            Map<String, List<String>> totalTopicExtractMap = new HashMap<>();
            for (String key:topicDoc.get(i).keySet()) {
                Map<String, List<String>> topicExtractMap = topicExtract(topicDoc.get(i).get(key),kArray[i],epochs, alpha, beta);
                totalTopicExtractMap.putAll(topicExtractMap);
                for (String key2:topicExtractMap.keySet()) {
                    for (String doc:topicExtractMap.get(key2)) {
                        List<String> preDocTopic = new ArrayList<>(docTopic.get(doc));
                        if (preDocTopic.size()<depth) {
                            if (!preDocTopic.contains(key2)) {
                                preDocTopic.add(key2);
                            }
                            docTopic.put(doc, preDocTopic);
                        }
                    }
                }

            }
            topicDoc.add(totalTopicExtractMap);
        }

        return docTopic;
    }

    private Map<String,List<String>> topicExtract(List<String> targets, int k, int epochs, double alpha, double beta) {
        Map<String,List<String>> ret = new HashMap<>();
        Map<String, Integer> words = new HashMap<>();
        List<List<Integer>> topics = new LinkedList<>();
        List<int[]> documentWordTopic = new LinkedList<>();
        Map<String, Integer>[] topicWord = new HashMap[k];
        String[] topicRep = new String[k];
        List<String>[] resTopics = new List[k];

        for (int i=0; i<k; i++) {
            topicWord[i] = new HashMap<>();
        }

        for (int i=0; i<targets.size(); i++) {
            documentWordTopic.add(new int[k]);
            String[] sentenceSplit = targets.get(i).split(" ");
            topics.add(new LinkedList<>());
            for (int j=0; j<sentenceSplit.length; j++) {
                String word = sentenceSplit[j];
                int randomTopic = (int) (Math.random() * k);
                topics.get(i).add(randomTopic);
                if (!words.containsKey(word)) {
                    words.put(word,1);
                } else {
                    words.put(word,words.get(word)+1);
                }
                documentWordTopic.get(i)[randomTopic]++;
                for (int l=0; l<k; l++) {
                    if (l==randomTopic) {
                        if (topicWord[l].containsKey(word)) {
                            topicWord[l].put(word, topicWord[l].get(word) + 1);
                        } else {
                            topicWord[l].put(word, 1);
                        }
                    } else {
                        if (!topicWord[l].containsKey(word)) {
                            topicWord[l].put(word, 0);
                        }
                    }
                }
            }
        }

        for (int epoch=0; epoch<epochs; epoch++) {
            for (int i=0; i<targets.size(); i++) {
                String[] sentenceSplit = targets.get(i).split(" ");
                for (int j=0; j<sentenceSplit.length; j++) {
                    List<Double> scores = new LinkedList<>();
                    String word = sentenceSplit[j];
                    int topic = topics.get(i).get(j);
                    documentWordTopic.get(i)[topic]--;
                    topicWord[topic].put(word,topicWord[topic].get(word)-1);
                    int documentTopicSum = 0;
                    for (int l=0; l<k; l++) {
                        documentTopicSum+=documentWordTopic.get(i)[l];
                    }
                    for (int l=0; l<k; l++) {
                        int topicWordSum = 0;
                        for (String key:words.keySet()) {
                            topicWordSum+=topicWord[l].get(key);
                        }
                        double score = ((double)documentWordTopic.get(i)[l] + alpha/k)/(documentTopicSum+alpha) * ((double)topicWord[l].get(word)+beta)/((double)topicWordSum+beta*words.size());
                        scores.add(score);
                    }
                    int idx = sampling(scores);
                    documentWordTopic.get(i)[idx]++;
                    topicWord[idx].put(word,topicWord[idx].get(word)+1);
                    topics.get(i).set(j,idx);
                }
            }
        }

        for (int i=0; i<k; i++) {
            topicRep[i] = findMax(topicWord[i]);
        }

        for (int i=0; i<k; i++) {
            resTopics[i] = new LinkedList<>();
        }

        for (int i=0; i<targets.size(); i++) {
            resTopics[findMax(documentWordTopic.get(i))].add(targets.get(i));
        }

        for (int i=0; i<k; i++) {
            ret.put(topicRep[i],resTopics[i]);
        }
        return ret;
    }

    private int findMax(int[] intArray) {
        int max = 0;
        int maxIdx = 0;
        for (int i=0; i<intArray.length; i++) {
            if (max<=intArray[i]) {
                max = intArray[i];
                maxIdx = i;
            }
        }
        return maxIdx;
    }

    private String findMax(Map<String, Integer> stringScore) {
        int max = 0;
        String maxKey = "";
        for (String key:stringScore.keySet()) {
            if (max<=stringScore.get(key)) {
                max = stringScore.get(key);
                maxKey = key;
            }
        }
        return maxKey;
    }

    public int sampling(List<Double> weights) {
        double sum = 0;
        for (int i=0; i<weights.size(); i++) {
            sum+=weights.get(i);
        }
        double random = Math.random() * sum;
        for (int i=0; i<weights.size(); i++) {
            random-=weights.get(i);
            if (random<=0) {
                return i;
            }
        }
        return weights.size()-1;
    }
}
