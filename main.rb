# calculate the total number of bigram and make bigram frequency table
bigram_fre = File.open("dataset/bigram_frequency.txt", "r")
no_bigram = 0
bigram_frequency = Hash.new()
while(line = bigram_fre.gets)
    list = line.split
    i = list[0]
    num = list[1].to_i
    bigram_frequency[i] = num
    no_bigram += num
end

# the number of possible bigrams and make unigram frequency table
unigram_fre = File.open("dataset/unigram_frequency.txt", "r")
unigram_frequency = Hash.new()
no_character = 0
length = 0
while(line = unigram_fre.gets)
    no_character += 1
    list = line.split
    i = list[0]
    num = list[1].to_i
    unigram_frequency[i] = num
    length = length + num
end
B = no_character * no_character

# take out pinyin mapping and store them as hash data structure
pinyin_mapping = File.open("dataset/pinyin_map.txt", "r")
pinyin = Hash.new()
while(line = pinyin_mapping.gets)
        list = line.split
        i = list[0]
        list = list.drop(1)
        list = list[0].gsub(/,/," ")
        char_list = list.split
        pinyin[i] = char_list
end

# get user query and form different combination
combination = Hash.new()
print "Input a query: "
query = gets
list = query.split
query_map = Hash.new()
list.each do |p|
    query_map[p] = pinyin[p]
end

# make different chinese character combination of user input
wordlist = []
wordlist = query_map[list[0]]

list.delete_at(0)
list.each do |q|
    candidate_list = []
    wordlist.each do |w|
        query_map[q].each do |candidate|
            candidate_list << w+candidate
        end
    end
    wordlist = candidate_list
end

# compute the probability of each combination based on language model
prob_wordlist = []
wordlist.each do |combo|
    i = 0
    list = []
    size = combo.size
    while i < size
        list << combo.slice(i,1)
        i = i + 1
    end
    uni_fre = unigram_frequency.values_at(list[0])
    k = uni_fre[0].to_f
    pro = k * 100000000000 / length.to_f
    last = list[0]
    list.delete_at(0)
    list.each do |c|
        bi_word = last + c
        last = c
        if bigram_frequency.has_key?(bi_word)
            bi_fre = bigram_frequency.values_at(bi_word)
            j = bi_fre[0].to_f
            pro = pro *(1.0 + j) * 100000000000/ (B.to_f + no_bigram.to_f)
        else
            pro = pro * 1 * 100000000000/ (B.to_f + no_bigram.to_f)
        end
    end
    prob_wordlist << pro
end

# store the result in to a hash data structure
i = 0
no_combo = wordlist.size
result = Hash.new()
while i < no_combo
    result[wordlist[i]] = prob_wordlist[i]
    i = i + 1
end

temp = result.sort_by{|key, value| value}.reverse
if temp.size < 10
    n = temp.size - 1
else
    n = 10
end
result_array = Array.new()
temp[0..n].each{|f|  result_array << f[0]}
puts result_array