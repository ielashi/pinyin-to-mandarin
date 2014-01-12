class LanguageModel
    # store frequency of the file into a hash
    def Frequency(file)
        fre = File.open( file , "r")
        frequency = Hash.new()
        while(line = fre.gets)
            list = line.split
            i = list[0]
            frequency[i] = list[1].to_i
        end
        return frequency
    end
    # sum up the total frequency of the file
    def SumUp(file)
        fre = File.open(file, "r")
        no = 0
        while(line = fre.gets)
            list = line.split
            no += list[1].to_i
        end
        return no
    end
    # take out pinyin mapping and store them as hash data structure
    def PinyinMap(file)
        pinyin_mapping= File.open(file, "r")
        pinyin = Hash.new()
        while(line = pinyin_mapping.gets)
            list = line.split
            i = list[0]
            list = list.drop(1)
            list = list[0].gsub(/,/," ")
            char_list = list.split
            pinyin[i] = char_list
        end
        return pinyin
    end
    # total number of possible bigrams
    def PossibleBigram
        no_character = 0
        fre = File.open("dataset/unigram_frequency.txt", "r")
        while (line = fre.gets)
            no_character += 1
        end
        return no_character * no_character
    end
    # try all combination
    def Combination(query_list,query_map)
        wordlist = []
        wordlist = query_map[query_list[0]]
        query_list.delete_at(0)
        query_list.each do |q|
            candidate_list = []
            wordlist.each do |w|
                query_map[q].each do |candidate|
                    candidate_list << w+candidate
                end
            end
            wordlist = candidate_list
        end
        return wordlist
    end
    
    def Cond_prob(last, this)
        last_fre = $unigram_frequency.values_at(last)
        last_fre = last_fre[0].to_f
        bi_word = last + this
        if $bigram_frequency.has_key?(bi_word)
            bi_fre = $bigram_frequency.values_at(bi_word)
            bi_frequency = bi_fre[0].to_f
            pro = (1.0 + bi_frequency) / ($possible_bigram.to_f + last_fre.to_f)
            pro = Math.log(pro)
        else
            pro = 1 / ($possible_bigram.to_f + last_fre.to_f)
            pro = Math.log(pro)
        end
        return pro
    end
    
    def ComputeProb(word)
        i = 0
        list = []
        size = word.size
        while i < size
            list << word.slice(i,1)
            i += 1
        end
        fre1 = $unigram_frequency.values_at(list[0])
        fre = fre1[0].to_f
        pro = fre / $total_unigram.to_f
        pro = Math.log(pro)
        last = list[0]
        list.delete_at(0)
        list.each do |this|
            pro = pro + Cond_prob(last,this)
            last = this
        end
        return pro
    end
end

##### main #####
#initialize data
model = LanguageModel.new
$bigram_frequency = model.Frequency("dataset/bigram_frequency.txt")
$total_bigram = model.SumUp("dataset/bigram_frequency.txt")
$unigram_frequency = model.Frequency("dataset/unigram_frequency.txt")
$total_unigram = model.SumUp("dataset/unigram_frequency.txt")
$pinyin_mapping = model.PinyinMap("dataset/pinyin_map.txt")
$possible_bigram = model.PossibleBigram

# get user query and form different combination
print "Input a query: "
query = gets
query_list = query.split
query_map = Hash.new()
query_list.each do |p|
    query_map[p] = $pinyin_mapping[p]
end

pro_wordlist = []
wordlist = model.Combination(query_list,query_map)
wordlist.each do |word|
    word_prob = model.ComputeProb(word)
    pro_wordlist << word_prob
end

i = 0
no_combo = wordlist.size
result = Hash.new()
while i < no_combo
    result[wordlist[i]] = pro_wordlist[i]
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
#puts temp
puts result_array
##### main ends #####