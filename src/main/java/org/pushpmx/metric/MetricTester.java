package org.pushpmx.metric;

import java.util.Arrays;
import java.util.Locale;

import org.ecj.psh.PshEvolutionState;
import org.ecj.psh.PshIndividual;
import org.pushpmx.Semantics;
import org.pushpmx.Semantics.CompareType;
import org.pushpmx.problem.FloatSymbolicRegression;
import org.spiderland.Psh.InterpreterState;
import org.spiderland.Psh.Program;
import org.spiderland.Psh.SemanticInterpreter;

import ec.EvolutionState;
import ec.Evolve;
import ec.Individual;
import ec.gp.koza.KozaFitness;
import ec.util.MersenneTwisterFast;
import ec.util.Output;
import ec.util.Parameter;
import ec.util.ParameterDatabase;

public class MetricTester {

	private static int ecjArgStart = 0;
	private SemanticsMetric metric = null;

	public static boolean equalsClose(double a, double b, double eps) {
		if (a > 1.e35)
			a = Double.POSITIVE_INFINITY;
		if (b > 1.e35)
			b = Double.POSITIVE_INFINITY;
		if (Double.isInfinite(a) && Double.isInfinite(b))
			return true;
		if (Double.isNaN(a)) {
			System.out.println("uwaga: a = " + a);
		}
		if (Double.isNaN(b)) {
			System.out.println("uwaga: b = " + b);
		}

		// System.out.println("Math.abs(a - b) = " + Math.abs(a - b));
		// System.out.println("Math.max(Math.abs(a), Math.abs(b) = " +
		// Math.max(Math.abs(a), Math.abs(b)));

		if (a == b
				|| Math.abs(a - b) /* / Math.max(Math.abs(a), Math.abs(b)) */< eps) {
			return true;
		}
		return false;
	}

	public static void checkMetricProperties(PshEvolutionState state) {
		Individual[] inds = state.population.subpops[0].individuals;

		SemanticsMetric[] metrics = new SemanticsMetric[] {
				new HammingMetric(),
				null,
				// new EuclideanMetric(),
				new PeekMetric(), 
				new PeekMetric(),
				new DiscordanceMetric() };

		// hamming
		((HammingMetric) metrics[0]).epsilon = Semantics.epsilon;

		// // euclidean
		// ((EuclideanMetric) metrics[1]).epsilon = Semantics.epsilon;

		// peek
		((PeekMetric) metrics[2]).epsilon = Semantics.epsilon;
		((PeekMetric) metrics[2]).type = PeekMetric.C_HAMMING;

		// peak 2
		((PeekMetric) metrics[3]).epsilon = Semantics.epsilon;
		((PeekMetric) metrics[3]).type = PeekMetric.C_EUCLIDEAN;
		
		// discordance
		((DiscordanceMetric) metrics[4]).epsilon = Semantics.epsilon;
		((DiscordanceMetric) metrics[4]).compareTails = true;

		// get the problem
		FloatSymbolicRegression problem = (FloatSymbolicRegression) state.evaluator.p_problem
				.clone();

		// get the interpreter
		SemanticInterpreter interpreter = (SemanticInterpreter) state.interpreter[0];

		// compute semantics
		Semantics[] semantics = new Semantics[inds.length];
		for (int i = 0; i < inds.length; i++) {
			PshIndividual pushInd = (PshIndividual) inds[i];
			InterpreterState[] memoryState = problem
					.computeInterpreterStateArray(
							problem.initInterpreterStateArray(), interpreter,
							pushInd.program);
			semantics[i] = new Semantics(memoryState);
		}

		// SemanticsMetric metric = metrics[0]; // hamming

		for (SemanticsMetric metric : metrics) {
			if (metric == null) continue;
			System.out.println(metric);
			// test the Hamming metric

			for (int i = 0; i < 10000; i++) {
				// peek individuals randomly
				int ind1 = state.random[0].nextInt(inds.length);
				int ind2 = state.random[0].nextInt(inds.length);
				int ind3 = state.random[0].nextInt(inds.length);

				double distance_1_2 = metric.getDistance(semantics[ind1],
						semantics[ind2]);
				double distance_2_1 = metric.getDistance(semantics[ind2],
						semantics[ind1]);
				
				boolean ident_1_2 = false;;
				if (metric instanceof HammingMetric)
					ident_1_2 = semantics[ind1].equals(semantics[ind2]);
				else if (metric instanceof DiscordanceMetric)
					ident_1_2 = semantics[ind1].equalParts(semantics[ind2],
							CompareType.StackTails);
				else if (metric instanceof PeekMetric)
					ident_1_2 = semantics[ind1].equalPeeks(semantics[ind2]);
				else {
					System.out.println("metryka nieznana");
					return;
				}
				
				double distance_1_3 = metric.getDistance(semantics[ind1],
						semantics[ind3]);
				double distance_2_3 = metric.getDistance(semantics[ind2],
						semantics[ind3]);

				//
				// double distance_3_1 = metric.getDistance(semantics[ind3],
				// semantics[ind1]);
				// boolean ident_1_3 = semantics[ind1].equals(semantics[ind3]);
				//
				// double distance_3_2 = metric.getDistance(semantics[ind3],
				// semantics[ind2]);
				// boolean ident_2_3 = semantics[ind2].equals(semantics[ind3]);

				// nieujemność
				if (distance_1_2 < 0) {
					System.out.println("Naruszona nieujemność: d(1,2)="
							+ distance_1_2);
					System.out.println("s1: " + semantics[ind1]);
					System.out.println("s2: " + semantics[ind2]);
					return;
				}

				// symetria
				if (distance_1_2 != distance_2_1) {
					System.out.println("Naruszona symetria: d(1,2)="
							+ distance_1_2 + ", d(2_1)=" + distance_2_1);
					System.out.println("s1: " + semantics[ind1]);
					System.out.println("s2: " + semantics[ind2]);
					return;
				}

				// identyczność
				if (metric instanceof HammingMetric) {
					if (((equalsClose(distance_1_2, 0, metric.epsilon)) && (!ident_1_2))
							|| ((!equalsClose(distance_1_2, 0, metric.epsilon)) && ident_1_2)) {
						System.out.println("Naruszona identyczność: d(1,2)="
								+ distance_1_2 + ", identyczne? " + ident_1_2);
						System.out.println("s1: " + semantics[ind1]);
						System.out.println("s2: " + semantics[ind2]);
						return;
					}
				}

				// nierówność trójkąta

				if (!equalsClose(distance_1_3, distance_1_2 + distance_2_3,
						metric.epsilon)
						&& (distance_1_3 > distance_1_2 + distance_2_3)) {
					System.out.println("Naruszony warunek trójkąta: d(1,2)="
							+ distance_1_2 + ", d(2,3)=" + distance_2_3
							+ ", d(1,2)+d(2,3)="
							+ (distance_1_2 + distance_2_3));
					System.out.println("d(1,3)=" + distance_1_3);
					System.out.println("Różnica: d(1,2)+d(2,3)-d(1,3)="
							+ (distance_1_2 + distance_2_3 - distance_1_3));
//					System.out.println("s1: " + semantics[ind1]);
//					System.out.println("s2: " + semantics[ind2]);
//					System.out.println("s2: " + semantics[ind3]);
					return;
				}

			}
		}
	}
	
	public static void computeFDC(PshEvolutionState state) {
		Individual[] inds = state.population.subpops[0].individuals;

		SemanticsMetric[] metrics = new SemanticsMetric[] {
				new HammingMetric(), new PeekMetric(), new PeekMetric(),
				new DiscordanceMetric(), new EuclideanMetric(), new EuclideanMetric() };

		// hamming
		((HammingMetric) metrics[0]).epsilon = Semantics.epsilon;

		// // euclidean
		// ((EuclideanMetric) metrics[1]).epsilon = Semantics.epsilon;

		// peek1
		((PeekMetric) metrics[1]).epsilon = Semantics.epsilon;
		((PeekMetric) metrics[1]).type = PeekMetric.C_HAMMING;

		// peek2
		((PeekMetric) metrics[2]).epsilon = 0;
		((PeekMetric) metrics[2]).type = PeekMetric.C_EUCLIDEAN;
		
		// discordance
		((DiscordanceMetric) metrics[3]).epsilon = Semantics.epsilon;
		((DiscordanceMetric) metrics[3]).compareTails = true;
		
		// euklides
		((EuclideanMetric) metrics[4]).epsilon = Semantics.epsilon;
		((EuclideanMetric) metrics[4]).partial = false;
		
		// euklides2
		((EuclideanMetric) metrics[5]).epsilon = Semantics.epsilon;
		((EuclideanMetric) metrics[5]).partial = true;
		
		// get the problem
		FloatSymbolicRegression problem = (FloatSymbolicRegression) state.evaluator.p_problem
				.clone();

		// get the interpreter
		SemanticInterpreter interpreter = (SemanticInterpreter) state.interpreter[0];

		// compute semantics
		Semantics[] semantics = new Semantics[inds.length];
		for (int i = 0; i < inds.length; i++) {
			PshIndividual pushInd = (PshIndividual) inds[i];
			InterpreterState[] memoryState = problem
					.computeInterpreterStateArray(
							problem.initInterpreterStateArray(), interpreter,
							pushInd.program);
			semantics[i] = new Semantics(memoryState);
		}

		for (int i = 0; i < 5000; i++) {
			int ind1 = state.random[0].nextInt(inds.length);
			int ind2 = state.random[0].nextInt(inds.length);
			double fitnessDifference = Math
					.abs(((KozaFitness) inds[ind1].fitness).adjustedFitness()
							- ((KozaFitness) inds[ind2].fitness)
									.adjustedFitness());
			double hammingDistance = metrics[0].getDistance(semantics[ind1],
					semantics[ind2]);
			double peakDistance1 = metrics[1].getDistance(semantics[ind1],
					semantics[ind2]);
			double peakDistance2 = metrics[2].getDistance(semantics[ind1],
					semantics[ind2]);
			double discordanceDistance = metrics[3].getDistance(
					semantics[ind1], semantics[ind2]);
			double euclideanDistance1 = metrics[4].getDistance(
					semantics[ind1], semantics[ind2]);
			double euclideanDistance2 = metrics[5].getDistance(
					semantics[ind1], semantics[ind2]);
			System.out.format(Locale.US, "%.6f\t%.6f\t%.6f\t%.6f\t%.6f\t%.6f\t%.6f\n",
					fitnessDifference, hammingDistance, peakDistance1, peakDistance2,
					discordanceDistance, euclideanDistance1, euclideanDistance2);
		}
		
	}

	public static void main(String[] args) throws RuntimeException, Exception {

		if (args.length < ecjArgStart) {
			System.out
					.println("Usage: org.pushpmx.metric.MetricTester [ ecj-parameters ]");
			return;
		}
		//
		// SemanticInterpreter interpreter = new SemanticInterpreter();
		// interpreter.Initialize(new MersenneTwisterFast());
		// interpreter.SetInstructions(new Program("( float.* float.+ float"
		// + ".- float./ float.sin " + "float.cos float.ln float.exp "
		// + "float.dup float.erc )"));
		//
		// interpreter.RandomCode(10);

		ParameterDatabase parameters = Evolve.loadParameterDatabase(Arrays
				.copyOfRange(args, ecjArgStart, args.length));

		EvolutionState state = Evolve.initialize(parameters, 0);
		state.startFresh();
		
		state.evaluator.evaluatePopulation(state);
		computeFDC((PshEvolutionState) state);
//		checkMetricProperties((PshEvolutionState)state);

		state.initializer.initialPopulation(state, 0);

		Evolve.cleanup(state);
	}

}
