import copy
import json
from collections import namedtuple

import requests
from flask import Flask, request

app = Flask(__name__)

with open('config.json') as config_file:
    config = json.load(config_file, object_hook=lambda d: namedtuple('Config', d.keys())(*d.values()))


@app.route('/<string:path>', methods=['POST'])
def home(path):
    """

    :return:
    """
    print('path: %s' % path)

    team = request.args.get('team')
    print('team: %s' % team)

    payload = json.loads(request.form.to_dict()['payload'])
    print('payload: %s' % payload)

    response = None

    for r in config.relays:
        print(r)
        if r.path == '/%s' % path:
            for h in r.hooks:

                if team is not None and team.casefold() != h.team.casefold():
                    continue

                # copy for next changes
                new_payload = copy.deepcopy(payload)

                if hasattr(h, 'channel') and not 'channel' in payload:
                    new_payload['channel'] = h.channel

                # abort if no channel exists in new_payload
                if 'channel' not in new_payload:
                    continue

                print('send: %s' % new_payload)
                response = requests.post(h.url, json=new_payload, headers={'content-type': 'x-www-form-urlencoded'})

    if response is not None:
        return response.text, response.status_code, response.headers.items()
    else:
        return "Bad Request", 400


if __name__ == '__main__':
    app.run(host="0.0.0.0")
