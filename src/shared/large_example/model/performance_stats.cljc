(ns large-example.model.performance-stats)

;; Model for client/server data structure

{:system/id                           33
 :system/report-start-ms              1928347783
 :system/report-end-ms                1948347783
 :system/average-cpu                  0.46
 :system/average-block-in-per-second  0.6
 :system/average-block-out-per-second 9.2
 :system/cpu-samples                  [{:cpu/sample-id 1 :cpu/percent 0.98 :cpu/sample-time-ms 1928347398} ...]
 :system/block-io-samples             [{:io/sample-id 1 :io/blocks-in-per-second 45 :io/blocks-out-per-second 1} ...]
 }

(defn cpu-sample [id time-ms percent]
  {:cpu/sample-id id :cpu/percent percent :sample-time-ms time-ms})

(defn block-io-sample [id time-ms blockin-per-s blockout-per-s]
  {:io/sample-id             id :io/blocks-in-per-second blockin-per-s
   :sample-time-ms           time-ms
   :io/blocks-out-per-second blockout-per-s})

(defn system-report [id start-ms end-ms]
  {:system/id id :system/report-start-ms start-ms :system/report-end-ms end-ms})

(defn add-cpu-samples
  "Add CPU samples to the given report. This function will automatically compute and add the
  average CPU to the report. Any samples that fall outside of the report range will be filtered.

  Returns the report with the CPU samples and average added."
  [{:keys [system/report-start-ms system/report-end-ms] :as report} samples]
  (let [in-range? (fn [{:keys [sample-time-ms]}] (<= report-start-ms sample-time-ms report-end-ms))
        interesting-samples (filter in-range? samples)
        average (/ (reduce #(+ %1 (:cpu/percent %2)) 0 interesting-samples)
                   (count interesting-samples))]
    (assoc report :system/average-cpu average :system/cpu-samples interesting-samples)))

(defn add-io-samples
  "Add IO samples to the given report. This function will automatically compute and add the
  average in/out stats to the report. Any samples that fall outside of the report range will be filtered.

  Returns the report with the io samples and averages added."
  [{:keys [system/report-start-ms system/report-end-ms] :as report} samples]
  (let [in-range? (fn [{:keys [sample-time-ms]}] (<= report-start-ms sample-time-ms report-end-ms))
        interesting-samples (filter in-range? samples)
        nsamples (count interesting-samples)
        {:keys [in out]} (reduce (fn [{:keys [in out]} {:keys [io/blocks-in-per-second
                                                               io/blocks-out-per-second]}]
                                   {:in  (+ in blocks-in-per-second)
                                    :out (+ out blocks-out-per-second)}) {} interesting-samples)]
    (assoc report :system/average-block-in-per-second (/ in nsamples)
                  :system/average-block-out-per-second (/ out nsamples))))
