import click
import json
import logging
import numpy as np
import scipy
import sys
import time

from sklearn.ensemble import RandomForestRegressor
from sklearn.externals import joblib
from sklearn.model_selection import RandomizedSearchCV

logging.basicConfig(level=logging.INFO, stream=sys.stderr)


@click.group()
def cli():
    pass

@cli.command()
@click.argument('model', type=click.File('wb'))
def train(model):
    start = time.perf_counter()

    data = json.load(sys.stdin)

    weights = np.array([d['weight'] for d in data])
    xs = np.matrix([d['features'] for d in data])
    ys = np.array([d['label'] for d in data])

    param_grid = {
        'n_estimators': scipy.stats.randint(1, 101),
        'max_depth': scipy.stats.randint(1, 51)
        #'min_samples_split': scipy.stats.randint(1, 200)
    }

    regressor = RandomForestRegressor(random_state=42)

    m = RandomizedSearchCV(
        regressor, param_grid, n_iter=100, cv=3, random_state=42)

    m.fit(xs, ys, weights)

    joblib.dump(m, model)

    logging.info("Trained on {} inputs in {:.3f} seconds.".format(len(data), time.perf_counter() - start))


@cli.command()
@click.argument('model', type=click.File('rb'))
def predict(model):
    start = time.perf_counter()

    data = json.load(sys.stdin)

    xs = np.matrix([d['features'] for d in data])

    m = joblib.load(model)

    sys.stdout.write(json.dumps(list(m.predict(xs))))

    logging.info("Predicted {} inputs in {:.3f} seconds.".format(len(data), time.perf_counter() - start))


if __name__ == '__main__':
    cli()

