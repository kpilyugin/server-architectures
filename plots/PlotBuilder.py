import csv

import matplotlib.pyplot as plt

varying = ['ARRAY_SIZE', 'NUM_CLIENTS', 'DELAY']
var_names = ['Array size', 'Clients count', 'Delay']
metrics = ['Request time on server', ' client time on server', ' client working time ']
archs = ['TCP_SINGLE_THREAD', 'TCP_MULTI_THREAD', 'TCP_THREAD_POOL', 'TCP_NON_BLOCKING',
         'TCP_ASYNC', 'UDP_MULTI_THREAD', 'UDP_THREAD_POOL']
colors = ['red', 'orange', 'yellow', 'green', 'blue', 'pink', 'violet']


def get_range(folder, idx):
    with open(folder + '/params.txt') as params:
        lines = params.readlines()
        return [int(s) for s in lines[idx].split() if s.isdigit()]


def get_results(metric, folder, arch):
    with open(folder + '/' + arch + '.csv') as csvfile:
        reader = csv.DictReader(csvfile)
        return [row[metric] for row in reader]


for idx in range(len(varying)):
    folder = '../results/' + varying[idx]
    [var_from, var_to, var_step] = get_range(folder, idx)
    values = [x for x in range(var_from, var_to + 1, var_step)]
    for metric in metrics:
        plt.clf()
        for i in range(len(archs)):
            results = get_results(metric, folder, archs[i])
            plt.plot(values, results, color=colors[i], label=archs[i])
        plt.legend(loc=2, fontsize='x-small')
        plt.xlabel(var_names[idx])
        plt.ylabel(metric.lower())
        name = (var_names[idx] + ', ' + metric.lower() + '.png').replace('  ', ' ').replace(' .', '.')
        plt.savefig(name, dpi=100)
