import java.util.*;

public class LDA {
    public void run(List<String> targets, int k, int epochs, double alpha, double beta) {
        System.out.println(targets);
        List<String> words = new LinkedList<>();
        List<List<Integer>> topics = new LinkedList<>();
        List<int[]> documentTopic = new LinkedList<>();
        Map<String, Integer>[] topicWord = new Map[k];
        for (int i=0; i<k; i++) {
            topicWord[i] = new HashMap<>();
        }
        for (int i=0; i<targets.size(); i++) {
            documentTopic.add(new int[k]);
            String[] sentenceSplit = targets.get(i).split(" ");
            topics.add(new LinkedList<>());
            for (int j=0; j<sentenceSplit.length; j++) {
                String word = sentenceSplit[j];
                int randomTopic = (int) (Math.random() * k);
                topics.get(i).add(randomTopic);
                if (!words.contains(word)) {
                    words.add(sentenceSplit[j]);
                }
                documentTopic.get(i)[randomTopic]++;
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
        System.out.println(topics);
        System.out.println(topicWord[0]);
        System.out.println(topicWord[1]);

        for (int epoch=0; epoch<epochs; epoch++) {
            for (int i=0; i<targets.size(); i++) {
                String[] sentenceSplit = targets.get(i).split(" ");
                for (int j=0; j<sentenceSplit.length; j++) {
                    List<Double> scores = new LinkedList<>();
                    String word = sentenceSplit[j];
                    int topic = topics.get(i).get(j);
                    documentTopic.get(i)[topic]--;
                    topicWord[topic].put(word,topicWord[topic].get(word)-1);
                    int documentTopicSum = 0;
                    for (int l=0; l<k; l++) {
                        documentTopicSum+=documentTopic.get(i)[l];
                    }
                    for (int l=0; l<k; l++) {
                        int topicWordSum = 0;
                        for (int m=0; m<words.size(); m++) {
                            topicWordSum+=topicWord[l].get(words.get(m));
                        }
                        double score = ((double)documentTopic.get(i)[l] + alpha/k)/(documentTopicSum+alpha) * ((double)topicWord[l].get(word)+beta)/((double)topicWordSum+beta*words.size());
                        scores.add(score);
                    }
                    int idx = sampling(scores);
//                    System.out.println(k+","+idx);
                    documentTopic.get(i)[idx]++;
                    topicWord[idx].put(word,topicWord[idx].get(word)+1);
                    topics.get(i).set(j,idx);
                }
            }
        }

        List<String>[] resTopic = new List[k];
        for (int i=0; i<k; i++) {
            resTopic[i] = new LinkedList<>();
        }

        for (int i=0; i<topics.size(); i++) {
            String[] sentenceSplit = targets.get(i).split(" ");
            for (int j=0; j<topics.get(i).size(); j++) {
                if (!resTopic[topics.get(i).get(j)].contains(sentenceSplit[j])) {
                    resTopic[topics.get(i).get(j)].add(sentenceSplit[j]);
                }
            }
        }

        System.out.println(topics);
        System.out.println(topicWord[0]);
        System.out.println(topicWord[1]);

        for (int i=0; i<k; i++) {
            System.out.println(resTopic[i]);
        }

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
