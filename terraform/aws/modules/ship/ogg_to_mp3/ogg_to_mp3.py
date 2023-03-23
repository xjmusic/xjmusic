import boto3
import os
import subprocess
import tempfile
import urllib.parse
from botocore.exceptions import ClientError

s3 = boto3.client('s3')
target_bitrate = os.getenv('TARGET_BITRATE')

def lambda_handler(event, context):
    bucket = event['Records'][0]['s3']['bucket']['name']
    key = urllib.parse.unquote_plus(event['Records'][0]['s3']['object']['key'], encoding='utf-8')
    try:

        # get the source audio from S3 bucket to temp file
        with tempfile.NamedTemporaryFile(mode='w+b') as source_file:
            with tempfile.NamedTemporaryFile(mode='w+b') as target_file:
                s3.download_fileobj(bucket, key, source_file)

                # convert source to MP3
                cmd = '/opt/bin/ffmpeg -y -hide_banner -loglevel error -i {} -b:a {} -f mp3 {}'.format(source_file.name, target_bitrate, target_file.name)
                subprocess.run(cmd.split(), stdout=subprocess.PIPE).stdout.decode('utf-8')

                # ship target to S3 bucket
                target_key = key.replace('.ogg', '.mp3')
                response = s3.upload_file(target_file.name, bucket, target_key)

                print('Shipped {} to {}'.format(target_key, bucket))
                return 'OK'


    except Exception as e:
        print('Error processing object {} from bucket {}: {}'.format(key, bucket, e))
        raise e

