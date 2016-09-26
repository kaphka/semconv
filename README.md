# semconv

This Tool is used to extract relevant semantic data from the GND and DNB


## Usage

1. Download the ttl data files: [DNB-Datendienst](http://datendienst.dnb.de/cgi-bin/mabit.pl?userID=opendata&pass=opendata&cmd=login)
2. Create a TDB store with the [tdbloader](https://jena.apache.org/documentation/tdb/commands.html#tdbloader2) (This takes a lot of time)

```bash
tdbloader2 --loc /path/for/database GND.ttl
```
3. Run the script in Eclipse

## TODO

- change default configuration
- build settings
