import requests
import time

with open("./data.txt", mode = "wt", encoding="utf-8") as f:
    to_date = "2021-03-06 00:00:00"
    while (to_date.split()[0]!="2017-10-01"):
        res = requests.get("https://api.upbit.com/v1/candles/minutes/1?market=KRW-BTC&count=181",
                          params={"to":to_date})
        res = res.json()
        to_date = res[-1]["candle_date_time_utc"].replace("T", " ")
        for r in res:
            ust_time = r["candle_date_time_utc"]
            opening = str(r["opening_price"])
            high = str(r["high_price"])
            low = str(r["low_price"])
            trade = str(r["trade_price"])
            trade_price = str(r["candle_acc_trade_price"])
            trade_volume = str(r["candle_acc_trade_volume"])
            f.write("\t".join([ust_time, opening, high, low, trade, trade_price, trade_volume])+"\n")
        time.sleep(0.1)
        
        
with open("./data.txt", mode="rt", encoding="utf-8") as f:
    data = [line.replace("\n","") for line in f.readlines()]
    
data.reverse()
with open("./data2.txt", mode="wt", encoding="utf-8") as f:
    for d in data:
        f.write(d+"\n")
with open("./newest.txt", mode="wt", encoding="utf-8") as f:
    f.write(data[-1])
