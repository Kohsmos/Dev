import requests
import time

with open("./newest.txt", mode="rt", encoding="utf-8") as f:
    newest = [line.split("\t")[0] for line in f.readlines()][0]
day_res = []
params = ""
flag = True
print(newest)
while(flag):
    res = requests.get("https://api.upbit.com/v1/candles/minutes/1?market=KRW-BTC&count=180",
                      params={"to":params})
    res = res.json()
    params = res[-1]["candle_date_time_utc"].replace("T", " ")
    print(params)
    for r in res:
        ust_time = r["candle_date_time_utc"]
        opening = str(r["opening_price"])
        high = str(r["high_price"])
        low = str(r["low_price"])
        trade = str(r["trade_price"])
        trade_price = str(r["candle_acc_trade_price"])
        trade_volume = str(r["candle_acc_trade_volume"])
        if ust_time==newest:
            flag = False
            break
        day_res.append("\t".join([ust_time, opening, high, low, trade, trade_price, trade_volume]))
    time.sleep(0.1)
day_res.reverse()
with open("./data2.txt", mode="at", encoding="utf-8") as f:
    for line in day_res:
        f.write(line+"\n")
with open("./newest.txt", mode="wt", encoding="utf-8") as f:
    f.write(day_res[-1])
