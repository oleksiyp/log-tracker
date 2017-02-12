package log_tracker;

import java.util.*;
import java.util.stream.Collectors;

public class GrepFilter {
    class Trie {
        char c;
        boolean terminal;
        Trie [] nodes;

        public Trie(String prefix, Set<String> strings) {
            if (prefix.isEmpty()) {
                c = 0;
            } else {
                c = prefix.charAt(prefix.length() - 1);
            }

            List<Trie> trieList = indexNodes(prefix, strings);
            addItemsToHash(trieList);
        }

        private List<Trie> indexNodes(String prefix, Set<String> strings) {
            List<Trie> trieList = new ArrayList<>();
            boolean []chars = new boolean[65536];
            for (String str : strings) {
                if (!str.startsWith(prefix)) {
                    continue;
                }
                if (str.length() < prefix.length()) {
                    continue;
                }
                if (str.length() == prefix.length()) {
                    terminal = true;
                    continue;
                }
                char c = str.charAt(prefix.length());
                if (chars[c]) {
                    continue;
                }
                chars[c] = true;
                trieList.add(new Trie(prefix + c, strings));
            }
            return trieList;
        }

        private void addItemsToHash(List<Trie> trieList) {
            if (trieList.isEmpty()) {
                nodes = null;
                return;
            }
            int sz = trieList.size();
            sz *= 1.4;
            sz = (sz << 1) & (~sz);
            nodes = new Trie[sz];
            int mask = sz - 1;

            for (Trie trie : trieList) {
                int hash = trie.c & mask;
                while (nodes[hash] != null) {
                    hash++;
                    hash &= mask;
                }
                nodes[hash] = trie;
            }
        }

        public boolean isTerminal() {
            return terminal || nodes == null;
        }

        public Trie get(char c) {
            if (isTerminal()) {
                return null;
            }
            int mask = nodes.length - 1;
            int hash = c & mask;
            while (nodes[hash] != null && nodes[hash].c != c) {
                hash++;
                hash &= mask;
            }
            return nodes[hash];
        }

        @Override
        public String toString() {
            String s = nodes == null
                    ? ""
                    : Arrays.asList(this.nodes)
                    .stream()
                    .filter(val -> val != null)
                    .collect(Collectors.toList())
                    .toString();
            return (c == 0 ? '@' : c) + (isTerminal() ? "!" : "") + s;
        }
    }

    private Trie root;
    private List<Trie> active;
    private List<Trie> active2;

    public GrepFilter(Set<String> strings) {
        root = new Trie("", strings);
        active = new ArrayList<>();
        active.add(root);
        active2 = new ArrayList<>();
    }

    public boolean search(char c) {
        boolean result = false;

        active2.clear();
        for (Trie trie : active) {
            Trie nextNode = trie.get(c);
            if (nextNode == null) {
                continue;
            }
            if (nextNode.isTerminal()) {
                result = true;
            }
            active2.add(nextNode);
        }

        List<Trie> swap = active;
        active = active2;
        active2 = swap;

        active.add(root);

        return result;
    }
}
