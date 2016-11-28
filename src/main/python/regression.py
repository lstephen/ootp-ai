import click
import json
import logging
import numpy as np
import scipy
import sys
import time

from sklearn.ensemble import RandomForestRegressor
from sklearn.externals import joblib
from sklearn.feature_selection import SelectKBest, f_regression
from sklearn.model_selection import RandomizedSearchCV
from sklearn.pipeline import Pipeline

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
        'selection__k': scipy.stats.randint(1, len(data[0]['features'])),
        'regressor__n_estimators': scipy.stats.randint(1, 100),
        'regressor__max_depth': scipy.stats.randint(1, 50)
    }

    feature_selection = SelectKBest(f_regression)

    regressor = RandomForestRegressor(random_state=42, min_weight_fraction_leaf=0.01)

    pipeline = Pipeline(steps=[('selection', feature_selection), ('regressor', regressor)])

    m = RandomizedSearchCV(
        pipeline, param_grid, n_iter=100, cv=3, random_state=42)

    m.fit(xs, ys, weights)

    joblib.dump(m, model)

    logging.info("Trained on {} inputs in {:.3f} seconds.".format(len(data), time.perf_counter() - start))

    sys.stdout.write("Best Parameters: {}\n".format(m.best_params_))
    sys.stdout.write("Feature Scores: {}\n".format(np.round_(m.best_estimator_.named_steps['selection'].scores_), 3))
    sys.stdout.write("Feature Importances: {}\n".format(np.round_(m.best_estimator_.named_steps['regressor'].feature_importances_, 3)))


@cli.command()
@click.argument('model', type=click.File('rb'))
def predict(model):
    start = time.perf_counter()

    data = json.load(sys.stdin)

    if (len(data) == 0):
        predictions = []
    else:
        xs = np.matrix([d['features'] for d in data])

        m = joblib.load(model)

        predictions = list(m.predict(xs))

    sys.stdout.write(json.dumps(predictions))

    logging.info("Predicted {} inputs in {:.3f} seconds.".format(len(data), time.perf_counter() - start))


if __name__ == '__main__':
    cli()

