#!/bin/bash

rm /tmp/biller-dump.sql

mysqldump --host=37.187.153.76 --user=root --password=Gaming,.123 --port 13306 biller > /tmp/biller-dump.sql

echo "Created dump file /tmp/biller-dump.sql"


