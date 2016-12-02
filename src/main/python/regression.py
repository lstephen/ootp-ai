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
from sklearn.kernel_ridge import KernelRidge
from sklearn.linear_model import LinearRegression, Ridge
from sklearn.model_selection import cross_val_score, GridSearchCV, RandomizedSearchCV
from sklearn.pipeline import Pipeline

logging.basicConfig(level=logging.INFO, stream=sys.stderr)


class RandomForest:
    def __init__(self, xs, ys, weights):
        param_grid = {
            'selection__k': scipy.stats.randint(1, xs.shape[1] + 1),
            'regressor__n_estimators': scipy.stats.randint(1, 100),
            'regressor__max_depth': scipy.stats.randint(1, 50)
        }

        feature_selection = SelectKBest(f_regression)

        regressor = RandomForestRegressor(random_state=42, min_weight_fraction_leaf=0.01)

        pipeline = Pipeline(steps=[('selection', feature_selection), ('regressor', regressor)])

        self._cv = RandomizedSearchCV(
                pipeline, param_grid, n_iter=100, cv=3, random_state=42, fit_params={'regressor__sample_weight': weights})

        self._cv.fit(xs, ys)

    def pipeline(self):
        return self._cv.best_estimator_

    def estimator(self):
        return self.pipeline()

    def cross_val_score(self, xs, ys, weights):
        return np.mean(cross_val_score(self.pipeline(), xs, ys, fit_params={'regressor__sample_weight': weights}))

    def fit(self, xs, ys, weights):
        self.pipeline().fit(xs, ys, regressor__sample_weight=weights)

    def report(self, out):
        out.write("Best Parameters: {}\n".format(self._cv.best_params_))
        out.write("Feature Scores: {}\n".format(np.round_(self.pipeline().named_steps['selection'].scores_), 3))
        out.write("Feature Mask: {}\n".format(self.pipeline().named_steps['selection']._get_support_mask()))
        out.write("Feature Importances: {}\n".format(np.round_(self.pipeline().named_steps['regressor'].feature_importances_, 3)))

    def __repr__(self):
        return "RandomForest(...)"

class RidgeEstimator:
    def __init__(self, xs, ys, weights):
        param_grid = {
            'selection__k': list(range(1, xs.shape[1] + 1)),
            'regressor__alpha': [0.1, 1.0, 10.0]
        }

        feature_selection = SelectKBest(f_regression)

        regressor = Ridge(random_state=42)

        pipeline = Pipeline(steps=[('selection', feature_selection), ('regressor', regressor)])

        self._cv = GridSearchCV(pipeline, param_grid, fit_params={'regressor__sample_weight': weights})

        self._cv.fit(xs, ys)

    def pipeline(self):
        return self._cv.best_estimator_

    def estimator(self):
        return self.pipeline()

    def cross_val_score(self, xs, ys, weights):
        return np.mean(cross_val_score(self.pipeline(), xs, ys, fit_params={'regressor__sample_weight': weights}))

    def fit(self, xs, ys, weights):
        self.pipeline().fit(xs, ys, regressor__sample_weight=weights)

    def report(self, out):
        out.write("Best Parameters: {}\n".format(self._cv.best_params_))
        out.write("Feature Scores: {}\n".format(np.round_(self.pipeline().named_steps['selection'].scores_), 3))
        out.write("Coefficients: {}\n".format(np.round_(self.pipeline().named_steps['regressor'].coef_, 3)))

    def __repr__(self):
        return "Ridge(...)"

class KernelRidgeEstimator:
    def __init__(self, xs, ys, weights):
        param_grid = {
            'selection__k': list(range(1, xs.shape[1] + 1)),
            'regressor__alpha': [0.1, 1.0, 10.0]
        }

        feature_selection = SelectKBest(f_regression)

        regressor = KernelRidge()

        pipeline = Pipeline(steps=[('selection', feature_selection), ('regressor', regressor)])

        self._cv = GridSearchCV(pipeline, param_grid, fit_params={'regressor__sample_weight': weights})

        self._cv.fit(xs, ys)

    def pipeline(self):
        return self._cv.best_estimator_

    def estimator(self):
        return self.pipeline()

    def cross_val_score(self, xs, ys, weights):
        return np.mean(cross_val_score(self.pipeline(), xs, ys, fit_params={'regressor__sample_weight': weights}))

    def fit(self, xs, ys, weights):
        self.pipeline().fit(xs, ys, regressor__sample_weight=weights)

    def report(self, out):
        out.write("Best Parameters: {}\n".format(self._cv.best_params_))
        out.write("Feature Scores: {}\n".format(np.round_(self.pipeline().named_steps['selection'].scores_), 3))

    def __repr__(self):
        return "KernelRidge(...)"

class LinearRegressionEstimator:
    def __init__(self, xs, ys, weights):
        param_grid = {
            'selection__k': list(range(1, xs.shape[1] + 1))
        }

        feature_selection = SelectKBest(f_regression)

        regressor = LinearRegression()

        pipeline = Pipeline(steps=[('selection', feature_selection), ('regressor', regressor)])

        self._cv = GridSearchCV(pipeline, param_grid, fit_params={'regressor__sample_weight': weights})

        self._cv.fit(xs, ys)

    def pipeline(self):
        return self._cv.best_estimator_

    def estimator(self):
        return self.pipeline()

    def cross_val_score(self, xs, ys, weights):
        return np.mean(cross_val_score(self.pipeline(), xs, ys, fit_params={'regressor__sample_weight': weights}))

    def fit(self, xs, ys, weights):
        self.pipeline().fit(xs, ys, regressor__sample_weight=weights)

    def report(self, out):
        out.write("Best Parameters: {}\n".format(self._cv.best_params_))
        out.write("Feature Scores: {}\n".format(np.round_(self.pipeline().named_steps['selection'].scores_), 3))
        out.write("Coefficients: {}\n".format(np.round_(self.pipeline().named_steps['regressor'].coef_, 3)))

    def __repr__(self):
        return "LinearRegressionEstimator(...)"


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

    #estimators = [ (e.cross_val_score(xs, ys, weights), e) for e in [ RandomForest(xs, ys, weights), RidgeEstimator(xs, ys, weights), KernelRidgeEstimator(xs, ys, weights), LinearRegressionEstimator(xs, ys, weights) ] ]
    estimators = [ (e.cross_val_score(xs, ys, weights), e) for e in [ RandomForest(xs, ys, weights) ] ]

    best = sorted(estimators)[-1][1]

    best.fit(xs, ys, weights)

    joblib.dump(best.estimator(), model)

    logging.info("Trained on {} inputs in {:.3f} seconds.".format(len(data), time.perf_counter() - start))

    sys.stdout.write("Selected: {}\n".format(best.__class__.__name__))
    sys.stdout.write("Scores: {}\n".format(estimators))

    best.report(sys.stdout)


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

