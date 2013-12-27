# -*- coding: utf-8 -*-
"""Script to count the bigrams in the sentences of the corpus.

When the script terminates, the bigram frequency count is stored in
    dataset/bigram_frequency.txt
"""

from collections import defaultdict
from nltk import bigrams

CHARS_TO_REMOVE = set([
    '"', '）', '（', '；', '。', '，', '、', '：', '.', '《', '》', '＂',
    '！', '?', '？', '!', '．'])

bigram_frequency = defaultdict(int)

f = open('../dataset/sentences.txt')

for sentence in f:
  # Remove punctuations, spaces, and new lines
  sentence = sentence.strip()
  for c in CHARS_TO_REMOVE:
    sentence = sentence.replace(c, '')
  sentence = sentence.decode('utf-8')

  sentence_bigrams = ['%s%s' % (x[0], x[1]) for x in bigrams(sentence)]

  for bigram in sentence_bigrams:
    bigram_frequency[bigram] += 1


# Output the bigram frequency map
output = open('../dataset/bigram_frequency.txt', 'w')

bigram_frequency = sorted(
    ((v, k) for (k, v) in bigram_frequency.iteritems()), reverse=True)

for v, k in bigram_frequency:
  output.write('%s %s\n' % (k.encode('utf8'), v))

output.close()
