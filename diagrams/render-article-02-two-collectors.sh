#!/bin/zsh
set -euo pipefail

script_dir="${0:A:h}"
frames_dir="$(mktemp -d "${TMPDIR%/}/buildpulse-cold-flow.XXXXXX")"
trap 'rm -rf -- "$frames_dir"' EXIT
regular_font="/System/Library/Fonts/Supplemental/Arial.ttf"
bold_font="/System/Library/Fonts/Supplemental/Arial Bold.ttf"

stage_labels=(QUEUED COMPILE TESTS SCAN DEPLOY SUCCESS)
stage_x=(360 500 640 780 920 1060)

render_frame() {
    local frame_number="$1"
    local a_count="$2"
    local b_count="$3"
    local caption="$4"
    local output="$frames_dir/frame-${frame_number}.png"
    local -a args

    args=(
        -size 1200x675 xc:'#F7F4ED'
        -font "$bold_font" -fill '#101828' -pointsize 38 -gravity NorthWest
        -annotate +60+38 'ONE COLD FLOW. TWO COLLECTORS. TWO EXECUTIONS.'
        -font "$regular_font" -fill '#475467' -pointsize 21
        -annotate +60+88 'Each collect() call starts the upstream recipe again from the beginning.'
        -fill '#F4F3FF' -stroke '#D6BBFB' -strokewidth 2
        -draw 'roundrectangle 60,135 1140,225 20,20'
        -font "$bold_font" -fill '#6941C6' -stroke none -pointsize 17
        -annotate +88+154 'COLD FLOW DEFINITION'
        -font "$regular_font" -fill '#42307D' -pointsize 22
        -annotate +88+185 'flow {  QUEUED → COMPILE → TESTS → SCAN → DEPLOY → SUCCESS  }'
        -font "$bold_font" -fill '#087E8B' -pointsize 18
        -annotate +78+266 'COLLECTOR A'
        -font "$bold_font" -fill '#B54708' -pointsize 18
        -annotate +78+436 'COLLECTOR B'
        -fill '#FFFFFF' -stroke '#D0D5DD' -strokewidth 2
        -draw 'roundrectangle 60,295 1140,420 20,20'
        -draw 'roundrectangle 60,465 1140,590 20,20'
        -stroke '#D0D5DD' -strokewidth 5
        -draw 'line 340,340 1080,340'
        -draw 'line 340,510 1080,510'
    )

    local i x fill_a stroke_a fill_b stroke_b label_offset label_a_geometry label_b_geometry
    for i in {1..6}; do
        x="${stage_x[$i]}"
        label_offset=$((x - 600))
        if (( label_offset >= 0 )); then
            label_a_geometry="+${label_offset}+365"
            label_b_geometry="+${label_offset}+535"
        else
            label_a_geometry="${label_offset}+365"
            label_b_geometry="${label_offset}+535"
        fi
        if (( i <= a_count )); then
            fill_a='#12B3A8'
            stroke_a='#087E8B'
        else
            fill_a='#FFFFFF'
            stroke_a='#98A2B3'
        fi
        if (( i <= b_count )); then
            fill_b='#F79009'
            stroke_b='#B54708'
        else
            fill_b='#FFFFFF'
            stroke_b='#98A2B3'
        fi
        args+=(
            -fill "$fill_a" -stroke "$stroke_a" -strokewidth 3
            -draw "circle ${x},340 $((x + 17)),340"
            -fill "$fill_b" -stroke "$stroke_b" -strokewidth 3
            -draw "circle ${x},510 $((x + 17)),510"
            -font "$bold_font" -fill '#344054' -stroke none -pointsize 13 -gravity North
            -annotate "$label_a_geometry" "${stage_labels[$i]}"
            -annotate "$label_b_geometry" "${stage_labels[$i]}"
            -gravity NorthWest
        )
    done

    local a_status='IDLE — no collect()'
    local b_status='IDLE — no collect()'
    (( a_count > 0 && a_count < 6 )) && a_status='EXECUTION A RUNNING'
    (( a_count == 6 )) && a_status='EXECUTION A COMPLETE'
    (( b_count > 0 && b_count < 6 )) && b_status='EXECUTION B RUNNING'
    (( b_count == 6 )) && b_status='EXECUTION B COMPLETE'

    args+=(
        -font "$bold_font" -fill '#087E8B' -pointsize 15 -gravity NorthWest
        -annotate +80+322 "$a_status"
        -fill '#B54708' -annotate +80+492 "$b_status"
        -fill '#101828' -stroke none -draw 'roundrectangle 60,615 1140,660 14,14'
        -font "$bold_font" -fill '#FFFFFF' -pointsize 19 -gravity Center
        -annotate +0+296 "$caption"
    )

    magick "${args[@]}" "$output"
}

render_frame 00 0 0 'Flow defined. Producer idle. Zero executions.'
render_frame 01 1 0 'A calls collect(): Execution A starts at QUEUED.'
render_frame 02 2 0 'A receives COMPILE. B still has no execution.'
render_frame 03 3 1 'B calls collect(): Execution B starts separately at QUEUED.'
render_frame 04 4 2 'A and B now move independently through the same recipe.'
render_frame 05 5 3 'B does not join A. It follows its own ordered sequence.'
render_frame 06 6 4 'A completes. B continues because its collection is independent.'
render_frame 07 6 5 'Stopping or completing A does not stop B.'
render_frame 08 6 6 'Two collect() calls produced two complete executions.'

magick \
    -delay 110 "$frames_dir/frame-00.png" \
    -delay 95 "$frames_dir/frame-01.png" \
    -delay 95 "$frames_dir/frame-02.png" \
    -delay 130 "$frames_dir/frame-03.png" \
    -delay 95 "$frames_dir/frame-04.png" \
    -delay 95 "$frames_dir/frame-05.png" \
    -delay 110 "$frames_dir/frame-06.png" \
    -delay 95 "$frames_dir/frame-07.png" \
    -delay 250 "$frames_dir/frame-08.png" \
    -loop 0 -layers Optimize "$script_dir/article-02-two-collectors.gif"

echo "$frames_dir"
