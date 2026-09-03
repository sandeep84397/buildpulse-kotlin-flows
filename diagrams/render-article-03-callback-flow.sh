#!/bin/zsh
set -euo pipefail

script_dir="${0:A:h}"
frames_dir="$(mktemp -d "${TMPDIR%/}/buildpulse-callback-flow.XXXXXX")"
trap 'rm -rf -- "$frames_dir"' EXIT
regular_font="/System/Library/Fonts/Supplemental/Arial.ttf"
bold_font="/System/Library/Fonts/Supplemental/Arial Bold.ttf"

render_frame() {
    local frame_number="$1"
    local phase="$2"
    local listener_count="$3"
    local sdk_event="$4"
    local collector_value="$5"
    local caption="$6"
    local output="$frames_dir/frame-${frame_number}.png"
    local sdk_fill='#FFFFFF'
    local listener_fill='#FFFFFF'
    local bridge_fill='#FFFFFF'
    local collector_fill='#FFFFFF'
    local event_color='#98A2B3'
    local arrow_one='#D0D5DD'
    local arrow_two='#D0D5DD'
    local arrow_three='#D0D5DD'

    (( phase == 1 )) && listener_fill='#E8F8F4'
    (( phase == 2 )) && { sdk_fill='#FFF4E5'; listener_fill='#E8F8F4'; event_color='#B54708'; arrow_one='#F79009'; }
    (( phase == 3 )) && { listener_fill='#E8F8F4'; bridge_fill='#EEF4FF'; event_color='#B54708'; arrow_two='#6172F3'; }
    (( phase == 4 )) && { bridge_fill='#EEF4FF'; collector_fill='#E8F8F4'; event_color='#087E8B'; arrow_three='#12B3A8'; }
    (( phase == 5 )) && { sdk_fill='#FFF4E5'; listener_fill='#E8F8F4'; bridge_fill='#EEF4FF'; collector_fill='#E8F8F4'; event_color='#087E8B'; arrow_one='#F79009'; arrow_two='#6172F3'; arrow_three='#12B3A8'; }
    (( phase == 6 )) && { bridge_fill='#F4F3FF'; listener_fill='#FFF4E5'; event_color='#6941C6'; arrow_two='#6941C6'; }
    (( phase == 7 )) && { listener_fill='#FDECEC'; event_color='#B42318'; arrow_one='#D92D20'; }
    (( phase == 8 )) && { sdk_fill='#FFF4E5'; event_color='#B54708'; }

    magick \
        -size 1200x675 xc:'#F7F4ED' \
        -font "$bold_font" -fill '#101828' -pointsize 42 -gravity NorthWest \
        -annotate +60+38 'CALLBACKFLOW: LISTENER IN. FLOW OUT.' \
        -font "$regular_font" -fill '#475467' -pointsize 22 \
        -annotate +60+94 'A safe bridge must register, forward, and always unregister.' \
        -fill "$sdk_fill" -stroke '#B54708' -strokewidth 3 \
        -draw 'roundrectangle 60,205 270,380 20,20' \
        -fill "$listener_fill" -stroke '#087E8B' \
        -draw 'roundrectangle 340,205 550,380 20,20' \
        -fill "$bridge_fill" -stroke '#6172F3' \
        -draw 'roundrectangle 620,205 860,380 20,20' \
        -fill "$collector_fill" -stroke '#087E8B' \
        -draw 'roundrectangle 930,205 1140,380 20,20' \
        -font "$bold_font" -fill '#101828' -stroke none -pointsize 20 \
        -annotate +108+235 'CI SDK' \
        -annotate +375+235 'LISTENER' \
        -annotate +655+235 'callbackFlow' \
        -annotate +968+235 'COLLECTOR' \
        -font "$regular_font" -fill '#475467' -pointsize 17 \
        -annotate +92+292 "emits ${sdk_event}" \
        -annotate +370+292 'onBuildUpdated' \
        -annotate +672+292 'trySend(value)' \
        -annotate +965+292 "gets ${collector_value}" \
        -stroke "$arrow_one" -strokewidth 7 -fill none \
        -draw 'line 275,292 330,292' \
        -fill "$arrow_one" -stroke none -draw 'polygon 330,292 312,281 312,303' \
        -stroke "$arrow_two" -strokewidth 7 -fill none \
        -draw 'line 555,292 610,292' \
        -fill "$arrow_two" -stroke none -draw 'polygon 610,292 592,281 592,303' \
        -stroke "$arrow_three" -strokewidth 7 -fill none \
        -draw 'line 865,292 920,292' \
        -fill "$arrow_three" -stroke none -draw 'polygon 920,292 902,281 902,303' \
        -fill '#FFFFFF' -stroke '#D0D5DD' -strokewidth 2 \
        -draw 'roundrectangle 60,425 1140,555 18,18' \
        -font "$bold_font" -fill '#344054' -stroke none -pointsize 19 \
        -annotate +85+452 'LIFECYCLE' \
        -font "$regular_font" -pointsize 18 -fill "$event_color" \
        -annotate +85+492 "$caption" \
        -font "$bold_font" -fill '#101828' -pointsize 17 \
        -annotate +890+452 "ACTIVE LISTENERS: ${listener_count}" \
        -fill '#101828' -stroke none \
        -draw 'roundrectangle 60,590 1140,650 14,14' \
        -font "$bold_font" -fill '#FFFFFF' -pointsize 19 -gravity Center \
        -annotate +0+285 "$caption" \
        "$output"
}

render_frame 00 0 0 'QUEUED' '—' 'SDK callback before collect(): missed because no listener exists.'
render_frame 01 1 1 '—' '—' 'collect() starts: callbackFlow calls addListener().'
render_frame 02 2 1 'COMPILING' '—' 'The CI SDK invokes onBuildUpdated(COMPILING).'
render_frame 03 3 1 'COMPILING' '—' 'The listener uses trySend(COMPILING) inside callbackFlow.'
render_frame 04 4 1 'COMPILING' 'COMPILING' 'Collector receives COMPILING as a normal Flow value.'
render_frame 05 5 1 'TESTS' 'TESTS' 'Future callbacks follow the same listener → Flow → collector path.'
render_frame 06 6 1 '—' '—' 'Collector cancels: awaitClose begins cleanup.'
render_frame 07 7 0 '—' '—' 'awaitClose calls removeListener(). No listener remains.'
render_frame 08 8 0 'SCAN' '—' 'A callback after cancellation is missed: the bridge is closed.'

magick "$frames_dir/frame-05.png" "$script_dir/article-03-callback-flow.png"

magick \
    -delay 150 "$frames_dir/frame-00.png" \
    -delay 125 "$frames_dir/frame-01.png" \
    -delay 105 "$frames_dir/frame-02.png" \
    -delay 105 "$frames_dir/frame-03.png" \
    -delay 145 "$frames_dir/frame-04.png" \
    -delay 130 "$frames_dir/frame-05.png" \
    -delay 120 "$frames_dir/frame-06.png" \
    -delay 155 "$frames_dir/frame-07.png" \
    -delay 240 "$frames_dir/frame-08.png" \
    -loop 0 -layers Optimize "$script_dir/article-03-callback-flow.gif"
