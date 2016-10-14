from property import *

# Neuron common parameters
iaf_neuronparams = {'E_L': -70.,
                    'V_th': -50.,
                    'V_reset': -67.,
                    'C_m': 2.,
                    't_ref': 2.,
                    'V_m': -60.,
                    'tau_syn_ex': 1.,
                    'tau_syn_in': 1.33}

# Synapse common parameters
STDP_synapseparams = {
    'model': 'stdp_synapse',
    'tau_m': {'distribution': 'uniform', 'low': 15., 'high': 25.},
    'alpha': {'distribution': 'normal_clipped', 'low': 0.5, 'mu': 5.0, 'sigma': 1.0},
    'delay': {'distribution': 'uniform', 'low': 0.8, 'high': 2.5},
    'lambda': 0.5
}

# Glutamate synapse
STDP_synparams_Glu = dict({'delay': {'distribution': 'uniform',
                                     'low': 1,
                                     'high': 1.3},
                           'weight': w_Glu,
                           'Wmax': 70.}, **STDP_synapseparams)

# GABA synapse
STDP_synparams_GABA = dict({'delay': {'distribution': 'uniform',
                                      'low': 1.,
                                      'high': 1.3},
                            'weight': w_GABA,
                            'Wmax': -60.}, **STDP_synapseparams)

# Dopamine synapse common parameter
DOPA_synparams = {'delay': 1.}
# Dopamine exhibitory synapse
DOPA_synparams_ex = dict({'weight': w_DA_ex,
                          'Wmax': 100.,
                          'Wmin': 85.}, **DOPA_synparams)

# Dictionary of synapses with keys and their parameters
types = {Glu : (STDP_synparams_Glu,  w_Glu,  'Glu'),
         GABA: (STDP_synparams_GABA, w_GABA, 'GABA'),
         DA_ex: (DOPA_synparams_ex, w_DA_ex, 'DA_ex', dopa_model_ex)}

# Parameters for generator links
static_syn = {
    'model': 'static_synapse',
    'weight': w_Glu * 5,
    'delay': pg_delay
}

# Device parameters
multimeter_param = {'to_memory': True,
                    'to_file': False,
                    'withtime': True,
                    'interval': 0.1,
                    'record_from': ['V_m'],
                    'withgid': True}

detector_param = {'label': 'spikes',
                  'withtime': True,
                  'withgid': True,
                  'to_file': False,
                  'to_memory': True,
                  'scientific': True}